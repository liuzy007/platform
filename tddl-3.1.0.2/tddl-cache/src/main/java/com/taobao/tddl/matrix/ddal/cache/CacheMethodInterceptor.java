package com.taobao.tddl.matrix.ddal.cache;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.common.lang.StringUtil;
import com.taobao.common.tair.DataEntry;
import com.taobao.common.tair.Result;
import com.taobao.common.tair.ResultCode;
import com.taobao.common.tair.TairManager;

/**
 * 基于spring aop的cache拦截，只针对接口
 * @author hu.weih
 *
 */
public class CacheMethodInterceptor implements MethodInterceptor,CacheMethodInterceptorMBean {

	private Log log = LogFactory.getLog(getClass());

	private TairManager tairManager;

	private PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();

	public TairManager getTairManager() {
		return tairManager;
	}

	public void setTairManager(TairManager tairManager) {
		this.tairManager = tairManager;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		//获取方法是否有 GetCache标注
		GetCache an = invocation.getMethod().getAnnotation(GetCache.class);		
		if (an != null ) {
			Annotation[][] pas = invocation.getMethod().getParameterAnnotations();
			Object[] as = invocation.getArguments();
			Object key = getkey(as, pas);
			if( key != null){
				int area = an.cacheArea();
				int expire = an.expire();
				Object cacheres = null;

				try {

					Result<DataEntry> result = this.tairManager.get(area, key);

					if (result!=null&&result.isSuccess()) {

						
						DataEntry dataEntry = result.getValue();
						//当result!=null时，并不能保证dataEntry不为null,还是需要判断
						if(dataEntry!=null){
						 cacheres = dataEntry.getValue();						
						}
					}else{
						log.error("get key"+key+result.getRc().getMessage());
					}

				} catch (Exception e) {
					log.error(key, e);
				}

				if (cacheres == null) {
					Object dbres = invocation.proceed();
					if (dbres != null) {
						ResultCode rs=null;
						try {
							rs=this.tairManager.put(area, key, (Serializable) dbres,
									0, expire);
							if(!rs.isSuccess()){
								log.error("put key"+key+rs.getMessage());
							}

						} catch (Exception e) {
							log.error(key, e);
						}
					}else{
						//如果可以存null值
						 if(an.canCacheNull()){
							 ResultCode rs=null;
							 try {
								 rs=this.tairManager.put(area, key,NullCache.getInstance(),
											0, expire);
							} catch (Exception e) {
								log.error(key, e);
							}
							 
								if(!rs.isSuccess()){
									log.error("put  Null to key"+key+rs.getMessage());
								}
						 }
					}

					return dbres;

				} else {
					 //如果取出的是 NullCache
					 if(an.canCacheNull() && cacheres instanceof NullCache){
						 return null;
					 }
					return cacheres;
				}
			}else{
				//如果取不到key 直接执行方法
				return invocation.proceed();
				
			}
			

		}
		//获取方法是否有 InvalidCacheByKey标注
		InvalidCacheByKey cik=invocation.getMethod().getAnnotation(
				InvalidCacheByKey.class);
		if(cik!=null){
			Annotation[][] pas = invocation.getMethod().getParameterAnnotations();
			Object[] as = invocation.getArguments();
			Object key = getkey(as, pas);
			if( key != null){
				int area = cik.cacheArea();
				try {
					if (key!=null) {
						this.tairManager.delete(area, key);
					}

				} catch (Exception e) {
					throw new CacheAccessException(e);
				}
			}
			//都要执行最终的 方法
			return invocation.proceed();
			
		}
		
		//获取方法是否有InvalidCacheByIds
		InvalidCacheByIds icids=invocation.getMethod().getAnnotation(
				InvalidCacheByIds.class);
		if(icids!=null){
			Annotation[][] pas = invocation.getMethod().getParameterAnnotations();
			Object[] as = invocation.getArguments();
			Object key = getkey(as, pas);
			if( key != null){
				int area = icids.cacheArea();
				try {
					if (key!=null) {
						String keyids=String.valueOf(key);
						String[] ids=keyids.split(",");
						if(ids!=null && ids.length>0){
							for (int i = 0; i < ids.length; i++) {
								this.tairManager.delete(area, ids[i]);
							}
						}
						
					}

				} catch (Exception e) {
					throw new CacheAccessException(e);
				}
			}
			//都要执行最终的 方法
			return invocation.proceed();
		}

		//获取方法是否有 InvalidCache标注
		InvalidCache ci = invocation.getMethod().getAnnotation(
				InvalidCache.class);
		if (ci != null) {
			Annotation[][] pas = invocation.getMethod().getParameterAnnotations();
			Object[] as = invocation.getArguments();
			int area = ci.cacheArea();
			String keyfield = ci.idfield();
			if (keyfield != null) {
				//有多个field情况
				String[] tfields = keyfield.split(",");
				String thekey = "";
				for (int i = 0; i < tfields.length; i++) {
					Object to = getCacheObject(as, pas);
					if (to != null) {
						try {
							Object tores = getPropertyUtils().getNestedProperty(to,
									tfields[i]);
							if (tores != null) {
								if(StringUtil.isBlank(thekey)){
									thekey=tores.toString();
								}else{
									//与getkey 一致
									thekey = thekey +";"+ tores.toString();
								}
								
							}
						} catch (Exception e) {
							log.error(e,e);
						}

					}

				}				
				try {
					if (StringUtil.isNotBlank(thekey)) {
						this.tairManager.delete(area, thekey);
					}

				} catch (Exception e) {
					throw new CacheAccessException(e);
				}
			}
			return invocation.proceed();
		}
		
		{
			// add by yusen 2009-10-13
			InvalidMultiCacheByAreaAndKey methodAnno = invocation.getMethod().getAnnotation(
					InvalidMultiCacheByAreaAndKey.class);
			if (methodAnno != null) {
				Annotation[][] parameterAnnos = invocation.getMethod().getParameterAnnotations();
				Object[] args = invocation.getArguments();
				int size = parameterAnnos.length;
				
				//参数为0的时候，一般是误用InvalidMultiCacheByAreaAndKey注解的时候 直接返回执行方法体
				if (size == 0) {
					return invocation.proceed();
				}
				
				/* 1.获取方法参数中的注解对象 */
				Map<Integer, Annotation> maps = new HashMap<Integer, Annotation>();// key：参数的index   value：参数对应的注解对象
				for (int i = 0; i < size; i++) {
					for (Annotation anno : parameterAnnos[i]) {
						if (anno instanceof CacheAreaKey || anno instanceof CacheAreaAndFiled) {
							maps.put(Integer.valueOf(i), anno);
							break;
						}
					}
				}
				
				/* 2.循环遍历方法参数的注解，执行清空缓存操作 */
				Set<Integer> keySet = maps.keySet();
				for (Integer paraIndex : keySet) {
					Annotation argAnno = maps.get(paraIndex);
					
					/* 2.1 注解的参数是简单数据类型的时候 */
					if (argAnno instanceof CacheAreaKey) {
						int cacheArea = ((CacheAreaKey) maps.get(paraIndex)).cacheArea();
						Object key = args[paraIndex];
						try {
							ResultCode resultCode = this.tairManager.delete(cacheArea, key);
							if(!resultCode.isSuccess()){
								log.error("清空缓存失败，"+resultCode.getMessage());
							}
						} catch (Exception e) {
							throw new CacheAccessException(e);
						}
						
						/* 2.2 注解的参数是对象类型的时候 */
					} else if (argAnno instanceof CacheAreaAndFiled) {
						// 值的形式如："cacheArea:fieldName;cacheArea2:fieldName2;cacheArea3:fieldName3"
						String str = ((CacheAreaAndFiled) maps.get(paraIndex)).cacheAreaAndField();
						String[] strss = str.split(";");
						for (String areaKeyPair : strss) {
							String[] ss = areaKeyPair.split(":");
							if (ss.length != 2) {
								throw new CacheAccessException(
										"CacheAreaAndFiled's cacheAreaAndField value's format is wrong,eg:'cacheArea:fieldName'==>number:string");
							}

							int cacheArea = 0;
							try {
								cacheArea = Integer.valueOf(ss[0]);
							} catch (NumberFormatException e) {
								throw new CacheAccessException(e);
							}

							Object key = null;
							try{
								key = this.getPropertyUtils().getNestedProperty(args[paraIndex], ss[1]);
							}catch(Exception e){
								throw new CacheAccessException(e);
							}

							if (key != null) {
								try {
									ResultCode resultCode = this.tairManager.delete(cacheArea, key);
									if(!resultCode.isSuccess()){
										log.error("清空缓存失败，"+resultCode.getMessage());
									}
								} catch (Exception e) {
									throw new CacheAccessException(e);
								}
							}else{
								log.warn("清空缓存失败，清空缓存的key对象中的"+ss[1]+"属性值为NULL！");
							}
						}
					} 
				}
				return invocation.proceed();
			}
		}
		
		
		//获取方法是否有 InvalidMultiCache标注
		InvalidMultiCache mci = invocation.getMethod().getAnnotation(InvalidMultiCache.class);
		if (mci != null) {
			Annotation[][] pas = invocation.getMethod().getParameterAnnotations();
			Object[] as = invocation.getArguments();
			String tareas = mci.cacheArea();
			String[] tareaarray=tareas.split(",");
			
			String keyfield = mci.idfield();
			if (keyfield != null) {
				//有多个field情况
				String[] tfields = keyfield.split(",");
				String thekey = "";
				for (int i = 0; i < tfields.length; i++) {
					Object to = getCacheObject(as, pas);
					if (to != null) {
						try {
							Object tores = getPropertyUtils().getNestedProperty(to,
									tfields[i]);
							if (tores != null) {
								if(StringUtil.isBlank(thekey)){
									thekey=tores.toString();
								}else{
									//与getkey 一致
									thekey = thekey +";"+ tores.toString();
								}
								
							}
						} catch (Exception e) {
							log.error(e,e);
						}
					}

				}				
				try {
					if (StringUtil.isNotBlank(thekey)) {
						for (int i = 0; i < tareaarray.length; i++) {
							this.tairManager.delete(Integer.valueOf(tareaarray[i]), thekey);
						}
						
					}

				} catch (Exception e) {
					throw new CacheAccessException(e);
				}
			}
			return invocation.proceed();
		}
		
		//如果以上标签都不存在 直接执行方法
		return invocation.proceed();
	}

	/**
	 * 返回要取key的对象
	 * 
	 * @param as
	 * @param pas
	 * @return
	 */
	private Object getCacheObject(Object[] as, Annotation[][] pas) {
		Object res = null;
		for (int i = 0; i < as.length; i++) {
			Annotation[] tas = pas[i];
			boolean exsit = false;
			for (int j = 0; j < tas.length; j++) {
				if (tas[j] instanceof CacheInvalidObject) {
					exsit = true;
					break;
				}
			}
			if (exsit)
				return as[i];
		}
		return res;
	}

	/**
	 * 返回key
	 * 各个cachekey+";"
	 * @param as
	 * @param pas
	 * @return
	 */
	private Object getkey(Object[] as, Annotation[][] pas) {
		Object key = null;
		if (as != null) {
			for (int i = 0; i < as.length; i++) {
				Annotation[] tas = pas[i];
				if (tas != null) {
					if (canbeKey(tas)) {
						if (key == null) {
							key = as[i].toString();
						} else {
							key = key +";"+ as[i].toString();
						}
					}

				}
			}
		}
		return key;
	}

	private boolean canbeKey(Annotation[] tas) {
		boolean canbekey = false;
		for (int j = 0; j < tas.length; j++) {
			if (tas[j] instanceof CacheKey) {
				canbekey = true;
				break;
			}
		}
		return canbekey;
	}

	@SuppressWarnings("unused")
	private Annotation getAnnotationKey(Annotation[] tas) {
		Annotation res = null;
		for (int j = 0; j < tas.length; j++) {
			if (tas[j] instanceof CacheKey) {
				res = tas[j];
				break;
			}
		}
		return res;
	}

	public PropertyUtilsBean getPropertyUtils() {

		return propertyUtilsBean;
	}

	public String delete(int namespace, String key) {
		if(namespace<0 || StringUtil.isEmpty(key))return "param is invalid";
		
		try {
			ResultCode rescode=this.tairManager.delete(namespace, key);
			return rescode.getMessage();
		} catch (Exception e) {
			return e.getMessage();
		}
		
	}

	public String get(int namespace, String key) {
		
		if(namespace<0 || StringUtil.isEmpty(key))return "param is invalid";
		Object cacheres=null;
		try {
			Result<DataEntry> result = this.tairManager.get(namespace, key);
			if (result != null && result.isSuccess()) {
				
				DataEntry dataEntry = result.getValue();
				//当result!=null时，并不能保证dataEntry不为null,还是需要判断
				if(dataEntry!=null){
				 cacheres = dataEntry.getValue();
				}
			}else{
				return result.toString();
			}
		} catch (Exception e) {
			
		}
		return cacheres==null?"null":ToStringBuilder.reflectionToString(cacheres);
	}
	
	
	
	
	/****** ****************************  Test  **********************************/
	@InvalidMultiCacheByAreaAndKey
	public void aa(@CacheAreaKey(cacheArea=0) String a,@CacheAreaKey(cacheArea=1)String b,@CacheAreaAndFiled(cacheAreaAndField="2:id;4:age") Person person){
		
	}
	public static class Person{
		long id;
		String name;
		int age;
		
		public Person(long id,String name,int age){
			this.id=id;
			this.name=name;
			this.age=age;
		}
		
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
	}
	
	public static void main(String argss[]) throws Exception{
		CacheMethodInterceptor obj=new CacheMethodInterceptor();
		Method m=obj.getClass().getMethod("aa", new Class[]{String.class,String.class,Person.class});

		InvalidMultiCacheByAreaAndKey methodAnno = m.getAnnotation(
				InvalidMultiCacheByAreaAndKey.class);
		if (methodAnno != null) {
			Annotation[][] parameterAnnos = m.getParameterAnnotations();
			Object[] args = new Object[]{"11","22",new Person(1L,"yusen",23)};
			int size = parameterAnnos.length;

			// key：参数的index value：参数对应的注解对象
			Map<Integer, Annotation> maps = new HashMap<Integer, Annotation>();

			for (int i = 0; i < size; i++) {
				for (Annotation anno : parameterAnnos[i]) {
					if (anno instanceof CacheAreaKey || anno instanceof CacheAreaAndFiled) {
						maps.put(Integer.valueOf(i), anno);
						break;
					}
				}
			}
			Set<Integer> keySet = maps.keySet();
			for (Integer paraIndex : keySet) {
				Annotation argAnno = maps.get(paraIndex);
				if (argAnno instanceof CacheAreaKey) {
					int cacheArea = ((CacheAreaKey) maps.get(paraIndex)).cacheArea();
					Object key = args[paraIndex];
					try {
						System.out.println("tair delete "+cacheArea+":"+key);
//						this.tairManager.delete(cacheArea, key);
					} catch (Exception e) {
						throw new CacheAccessException(e);
					}
				} else if (argAnno instanceof CacheAreaAndFiled) {
					// 值的形式如："cacheArea:fieldName"
					String str = ((CacheAreaAndFiled) maps.get(paraIndex)).cacheAreaAndField();
					String[] strss = str.split(";");
					for (String areaKeyPair : strss) {
						String[] ss = areaKeyPair.split(":");
						if (ss.length != 2) {
							throw new CacheAccessException(
									"CacheAreaAndFiled's cacheAreaAndField value's format is wrong,eg:'cacheArea:fieldName'==>number:string");
						}

						int cacheArea = 0;
						try {
							cacheArea = Integer.valueOf(ss[0]);
						} catch (NumberFormatException e) {
							throw new CacheAccessException(e);
						}

						Object key = obj.getPropertyUtils().getNestedProperty(args[paraIndex], ss[1]);

						if (key != null) {
							try {
//								this.tairManager.delete(cacheArea, key);
								System.out.println("tair delete "+cacheArea+":"+key);
							} catch (Exception e) {
								throw new CacheAccessException(e);
							}
						}
					}

				} else {

				}
			}
		}
	}
}
