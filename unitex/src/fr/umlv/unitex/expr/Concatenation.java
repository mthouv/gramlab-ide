package fr.umlv.unitex.expr;

import java.util.List;

public class Concatenation extends Operator {

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Value)) {
			return false;
		}
		Concatenation c= (Concatenation) o;
		return c.getOperands().equals(this.getOperands());
	}
	
	
	@Override
	protected String getOpSymbol() {
		return " ";
	}
	
	
	
	public String getOpeningSymbol() {
		return "";
	}
	
	public String getClosingSymbol() {
		return "";
	}
	
	
	/*
	public void addOperand(Parallelization p) {
		super.addOperand(p);
	}
	*/
	
	/*
	public void addOperand(Value v) {
		super.addOperand(v);
	}
	*/
	
	/*
	public void addOperand(Value v) {
		this.addOperand(v);
	}
	*/
	
	/*
	public void addOperand(Concatenation c) {
		List<Expression> list = c.getOperands();
		for (Expression e : list) {
			this.addOperand(e);
		}
	}
	*/
	
	
	public void addOperand(Expression expr) {
		if (expr instanceof Concatenation) {
			for (Expression e : expr.getOperands()) {
				this.addOperand(e);
			}
		}
		else {
			super.addOperand(expr);
		}
	}
	
	
	public void addOperands(Expression expr, int start, int end) {
		List<Expression> list = expr.getOperands();
		for (int i = start; i < end; i++) {
			this.addOperand(list.get(i));
		}
	}

	
	
	
	/*
	public void addOperand(Expression expr, int start, int end) {
		List<Expression> list = expr.getOperands();
		for (int i = start; i < end; i++) {
			this.addOperand(list.get(i));
		}
	}
	*/
		

	
}
