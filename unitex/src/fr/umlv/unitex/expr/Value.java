package fr.umlv.unitex.expr;

import java.util.ArrayList;
import java.util.List;

public class Value implements Expression {
	
	private final String value;
	

	public Value(String value) {
		if (value.charAt(0) == '{') {
			this.value = value.substring(1, value.length() - 1);
		}
		else {
			this.value = value;
		}
	}
	
	
	@Override
	public String toString() {
		return value;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Value)) {
			return false;
		}
		Value v = (Value) o;
		if (v.value.equals(this.value)) {
			return true;
		}
		return false;
	}
	
	
	
	@Override
	public List<Expression> getOperands() {
		List<Expression> list = new ArrayList<Expression>();
		list.add(this);
		return list;
	}
	
	@Override
	public int getOperandsSize() {
		return 1;
	}

}
