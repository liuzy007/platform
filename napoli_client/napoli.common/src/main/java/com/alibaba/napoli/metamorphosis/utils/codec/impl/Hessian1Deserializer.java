/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Authors:
 *   wuhua <wq163@163.com> , boyan <killme2008@gmail.com>
 */
package com.alibaba.napoli.metamorphosis.utils.codec.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.alibaba.napoli.metamorphosis.utils.codec.Deserializer;
import com.alibaba.com.caucho.hessian.io.Hessian2Input;
//import com.caucho.hessian.io.HessianInput;


/**
 * 
 * @author wuxin
 * @since 1.0, 2009-10-20 上午10:03:15
 */
public class Hessian1Deserializer implements Deserializer {

    private final Logger logger = Logger.getLogger(Hessian1Deserializer.class);

    @Override
    public Object decodeObject(final byte[] in) throws IOException {
        Object obj = null;
        ByteArrayInputStream bais = null;
        Hessian2Input input = null;
        try {
            bais = new ByteArrayInputStream(in);
            input = new Hessian2Input(bais);
            input.startMessage();
            obj = input.readObject();
            input.completeMessage();
        }
        catch (final IOException ex) {
            throw ex;
        }
        catch (final Throwable e) {
            this.logger.error("Failed to decode object.", e);
        }
        finally {
            if (input != null) {
                try {
                    bais.close();
                }
                catch (final IOException ex) {
                    this.logger.error("Failed to close stream.", ex);
                }
            }
        }
        return obj;
    }
}