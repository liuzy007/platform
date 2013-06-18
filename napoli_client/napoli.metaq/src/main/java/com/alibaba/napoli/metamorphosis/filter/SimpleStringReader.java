package com.alibaba.napoli.metamorphosis.filter;

import java.io.IOException;
import java.io.Reader;

public class SimpleStringReader extends Reader
{

	   
	   // Attributes ----------------------------------------------------

	   private final SimpleString simpleString;

	   private int next = 0;

	   // Static --------------------------------------------------------

	   // Constructors --------------------------------------------------

	   public SimpleStringReader(final SimpleString simpleString)
	   {
	      this.simpleString = simpleString;
	   }

	   // Public --------------------------------------------------------

	   // Reader overrides ----------------------------------------------

	   @Override
	   public int read(final char[] cbuf, final int off, final int len) throws IOException
	   {
	      synchronized (simpleString)
	      {
	         if (off < 0 || off > cbuf.length || len < 0 || off + len > cbuf.length || off + len < 0)
	         {
	            throw new IndexOutOfBoundsException();
	         }
	         else if (len == 0)
	         {
	            return 0;
	         }
	         int length = simpleString.length();
	         if (next >= length)
	         {
	            return -1;
	         }
	         int n = Math.min(length - next, len);
	         simpleString.getChars(next, next + n, cbuf, off);
	         next += n;
	         return n;
	      }
	   }

	   @Override
	   public void close() throws IOException
	   {
	   }

	   // Package protected ---------------------------------------------

	   // Protected -----------------------------------------------------

	   // Private -------------------------------------------------------

	   // Inner classes -------------------------------------------------

	}