package com.alibaba.napoli.metamorphosis.filter;

import java.util.regex.Pattern;

public class RegExp {

	   private final Pattern re;

	   public RegExp(final String pattern, final Character escapeChar) throws Exception
	   {
	      String pat = adjustPattern(pattern, escapeChar);

	      re = Pattern.compile(pat);
	   }

	   public boolean isMatch(final Object target)
	   {
	      String str = target != null ? target.toString() : "";

	      return re.matcher(str).matches();
	   }

	   protected String adjustPattern(final String pattern, final Character escapeChar) throws Exception
	   {
	      int patternLen = pattern.length();

	      StringBuffer REpattern = new StringBuffer(patternLen + 10);

	      boolean useEscape = escapeChar != null;

	      char escape = Character.UNASSIGNED;

	      if (useEscape)
	      {
	         escape = escapeChar.charValue();
	      }

	      REpattern.append('^');

	      for (int i = 0; i < patternLen; i++)
	      {
	         boolean escaped = false;

	         char c = pattern.charAt(i);

	         if (useEscape && escape == c)
	         {
	            i++;

	            if (i < patternLen)
	            {
	               escaped = true;
	               c = pattern.charAt(i);
	            }
	            else
	            {
	               throw new Exception("LIKE ESCAPE: Bad use of escape character");
	            }
	         }

	         // Match characters, or escape ones special to the underlying
	         // regex engine
	         switch (c)
	         {
	            case '_':
	               if (escaped)
	               {
	                  REpattern.append(c);
	               }
	               else
	               {
	                  REpattern.append('.');
	               }
	               break;
	            case '%':
	               if (escaped)
	               {
	                  REpattern.append(c);
	               }
	               else
	               {
	                  REpattern.append(".*");
	               }
	               break;
	            case '*':
	            case '.':
	            case '\\':
	            case '^':
	            case '$':
	            case '[':
	            case ']':
	            case '(':
	            case ')':
	            case '+':
	            case '?':
	            case '{':
	            case '}':
	            case '|':
	               REpattern.append("\\");
	               REpattern.append(c);
	               break;
	            default:
	               REpattern.append(c);
	               break;
	         }
	      }

	      REpattern.append('$');
	      return REpattern.toString();
	   }

}
