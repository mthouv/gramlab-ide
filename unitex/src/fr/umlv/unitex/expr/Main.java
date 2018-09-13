package fr.umlv.unitex.expr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;



public class Main {
	
	public static void main(String[] args) throws IOException {
		
		Parallelization p = new Parallelization();
		Parallelization p2 = new Parallelization();
		Parallelization p3 = new Parallelization();


		Concatenation c = new Concatenation();
		Concatenation c2= new Concatenation();

		
		p.addOperand(new Value("le"));
		p.addOperand(new Value("la"));
		
		c.addOperand(p);
		c.addOperand(new Value("aaa"));
		
		System.out.println(c);
		
		c2.addOperand(p);
		c2.addOperand(new Value("bbb"));
		
		System.out.println(c2);
		
		p2.addOperand(c2);
		p2.addOperand(new Value("hello"));
		
		System.out.println(p2);
		
		
		p3.addOperand(c);
		p3.addOperand(p2);
		
		System.out.println(p3 + "\n");
		
		
		p3.simplify();
		System.out.println(p3);
		
		/*
		Concatenation c2 = new Concatenation();
		
		
		
		Parallelization p2 = new Parallelization();
				
		
		p2.addOperand(new Value("poisson"));
		p2.addOperand(new Value("verseau"));
		p2.addOperand(new Value("scorpion"));
		
		
		c.addOperand(new Value("bidule"));
		c.addOperand(new Value("machin"));
		c.addOperand(new Value("chose"));
		
		Expression e = c;
		
		c2.addOperand(new Value("hello"));
		c2.addOperand(new Value("bonjour"));
		c2.addOperand(e);
		
		c.addOperand(p2);
		
		
		p.addOperand(new Value("Hello"));
		p.addOperand(new Value("Coucou"));
		p.addOperand(new Value("Salut"));
		p.addOperand(c);
		
		
		// TODO
		p.addOperand(p2);
		
		System.out.println(c2.getOperandsSize());
		System.out.println(c2.getOperands().get(4));

		
		
		final File sentenceGrf = new File("cursentence.grf");
		
		System.out.println(sentenceGrf);
		
		FileReader fileReader = new FileReader(sentenceGrf);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		
		for (int i = 0; i < 25; i++) {
			System.out.println(bufferedReader.readLine());
		}
		
		bufferedReader.close();
		
		GraphIO g = GraphIO.loadGraph(sentenceGrf, true, true);
		
		*/
		
		
	}
}
