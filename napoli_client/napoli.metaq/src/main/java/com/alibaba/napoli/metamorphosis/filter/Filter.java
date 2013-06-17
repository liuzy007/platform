package com.alibaba.napoli.metamorphosis.filter;

import com.alibaba.napoli.metamorphosis.Message;

public interface Filter {

	
	boolean match(Message message);
}
