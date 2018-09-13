package fr.umlv.unitex.expr;

import java.util.List;

public class Parallelization extends Operator {
	
	
	
	@Override
	public String getOpSymbol() {
		return " | ";
	}
	
	
	public String getOpeningSymbol() {
		return "(";
	}
	
	public String getClosingSymbol() {
		return ")";
	}
	
	
	/*
	public void addOperand(Concatenation c) {
		super.addOperand(c);
	}
	
	
	
	public void addOperand(Value v) {
		super.addOperand(v);
	}
	
	
	
	public void addOperand(Parallelization p) {
		List<Expression> list = p.getOperands();
		for (Expression e : list) {
			this.addOperand(e);
		}
	}
	*/
	
	
	public void addOperand(Expression expr) {
		if (expr instanceof Parallelization) {
			for (Expression e : expr.getOperands()) {
				this.addOperand(e);
			}
		}
		else {
			super.addOperand(expr);
		}
	}
	
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Parallelization)) {
			return false;
		}
		Parallelization p = (Parallelization) o;
		return p.getOperands().equals(this.getOperands());
	}
	
	
	public void simplify() {
		List<Expression> operands = this.getOperands();
		Expression firstOperand = operands.get(0), tmpOperand;
		
		if (firstOperand.getOperandsSize() == 1) {
			return;
		}
		
		for (int i = 1; i < operands.size(); i++) {
			tmpOperand = operands.get(i);
			if (tmpOperand.getOperandsSize() != 1) {
				if (firstOperand.getOperands().get(0).toString().equals(tmpOperand.getOperands().get(0).toString())) {
					
					Concatenation concat = new Concatenation();
					concat.addOperand(firstOperand.getOperands().get(0));
					Parallelization tmpPara = new Parallelization();
					
					this.removeOperand(i);
					this.removeOperand(0);
					
					int j;
					for (j = 1; j < firstOperand.getOperandsSize() && j < tmpOperand.getOperandsSize(); j++) {
						if (firstOperand.getOperands().get(j).toString().equals(tmpOperand.getOperands().get(j).toString())) {
							concat.addOperand(firstOperand.getOperands().get(j));
						}
						else {
							break;
						}
					}
					
					//concat.addOperand(firstOperand.getOperands().get(0));
					
					tmpPara.addOperand(firstOperand.getOperands().get(j));
					tmpPara.addOperand(tmpOperand.getOperands().get(j));
					concat.addOperand(tmpPara);
					
					this.addOperand(concat);
					return;
				}
			}
		}
	}
	
	
	
	public Expression simplifyAsConcatenation() {
		if (this.getOperandsSize() != 2) {
			throw new IllegalStateException("Parallelization must contain exactly two operands to be simplified as a concatenation");
		}
		
		List<Expression> operands = this.getOperands();
		Expression firstOperand = operands.get(0), secondOperand = operands.get(1);
		
		int firstOperandSize = firstOperand.getOperandsSize(), secondOperandSize = secondOperand.getOperandsSize();
		
		if (firstOperandSize == 1 || secondOperandSize == 1) {
			return this;
		}
		
		/*
		System.out.println("PARA -> " + this + "\n");
		
		System.out.println("AAAAAAAAA");
		System.out.println("FIRST : " + firstOperand.getOperands().get(0).toString() +"\n");
		
		System.out.println("BBBBBBBBBBB");
		System.out.println("SECOND : " + secondOperand.getOperands().get(0).toString() +"\n");
		*/
		
		/*
		if (firstOperand.getOperands().get(0).getOperandsSize() == 0) {
			System.out.println("HAHAHHAAH");
		}
		if (secondOperand.getOperands().get(0).getOperandsSize() == 0) {
			System.out.println("BBBBB");
		}
		 */
		
		if (firstOperand.getOperands().get(0).toString().equals(secondOperand.getOperands().get(0).toString())) {
			Concatenation concat = new Concatenation();
			Parallelization tmpPara = new Parallelization();
			concat.addOperand(firstOperand.getOperands().get(0));
			
			
			if (firstOperandSize > 2) {
				Concatenation tmpConcat = new Concatenation();
				for (int i = 1; i < firstOperandSize; i++) {
					tmpConcat.addOperand(firstOperand.getOperands().get(i));
				}
				tmpPara.addOperand(tmpConcat);
			}
			else {
				tmpPara.addOperand(firstOperand.getOperands().get(1));
			}
			
			
			if (secondOperandSize > 2) {
				Concatenation tmpConcat = new Concatenation();
				for (int i = 1; i < secondOperandSize; i++) {
					tmpConcat.addOperand(secondOperand.getOperands().get(i));
				}
				tmpPara.addOperand(tmpConcat);
			}
			else {
				tmpPara.addOperand(secondOperand.getOperands().get(1));
			}
			
			concat.addOperand(tmpPara);
			
			return concat;
		}
		
		return this;
	}
	
	
	/*public void addOperand(Expression expr, int start, int end) {
		List<Expression> list = expr.getOperands();
		for (int i = start; i < end; i++) {
			this.addOperand(list.get(i));
		}
	}*/


}
