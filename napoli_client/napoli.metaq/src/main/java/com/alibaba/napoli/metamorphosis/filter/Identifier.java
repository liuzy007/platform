package com.alibaba.napoli.metamorphosis.filter;

public class Identifier {

	private final SimpleString name;

	private final int hash;

	private Object value;

	public Identifier(final SimpleString name) {
		this.name = name;

		hash = name.hashCode();

		value = null;
	}

	@Override
	public String toString() {
		return "Identifier@" + name;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Identifier)) {
			return false;
		}
		if (obj.hashCode() != hash) {
			return false;
		}
		return ((Identifier) obj).name.equals(name);
	}

	@Override
	public int hashCode() {
		return hash;
	}

	public SimpleString getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(final Object value) {
		this.value = value;
	}

}
