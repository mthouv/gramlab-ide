package fr.umlv.unitex.expr;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.umlv.unitex.graphrendering.GenericGraphBox;
import fr.umlv.unitex.graphrendering.TfstGraphBox;
import fr.umlv.unitex.io.GraphIO;

public class Kleene {

	private final HashMap<SimpleEntry<Integer, Integer>, Expression> map = new HashMap<>();
	private final HashMap<SimpleEntry<Integer, Integer>, Expression> tmpMap = new HashMap<>();
	private final GraphIO graph;



	public Kleene(GraphIO graph) {
		this.graph = graph;
	}


	@SuppressWarnings("unchecked")
	ArrayList<Integer>[] computeReverseTransitions(GraphIO graph) {
		final ArrayList<Integer>[] reverse = new ArrayList[graph.getBoxes().size()];
		for (int i = 0; i < reverse.length; i++) {
			reverse[i] = new ArrayList<Integer>();
		}
		for (int i = 0; i < reverse.length; i++) {
			final TfstGraphBox box = (TfstGraphBox) graph.getBoxes().get(i);
			if (box == null)
				continue;
			/* We explore all outgoing transitions */
			for (final GenericGraphBox gb : box.transitions) {
				final int destIndex = getBoxIndex((TfstGraphBox) gb, graph.getBoxes());
				reverse[destIndex].add(i);
			}
		}
		return reverse;
	}


	int getBoxIndex(TfstGraphBox b, ArrayList<GenericGraphBox> boxes) {
		if (b == null)
			return -1;
		for (int i = 0; i < boxes.size(); i++) {
			if (b.equals(boxes.get(i)))
				return i;
		}
		// If the box is unknown then we return -1
		return -1;
	}



	private List<Integer> topologicalSort(GraphIO graph) {
		ArrayList<GenericGraphBox> boxes = graph.getBoxes();
		int[] renumber = new int[boxes.size()];
		final int[] incoming = new int[boxes.size()];
		final ArrayList<Integer>[] reverse = computeReverseTransitions(graph);
		for (int i = 0; i < incoming.length; i++) {
			incoming[i] = reverse[i].size();
		}
		/*
		 * We set up the renumber array so that renumber[q] will give us the
		 * rank of the node #q after the topological sort
		 */
		for (int q = 0; q < incoming.length; q++) {
			int old = 0;
			while (old < incoming.length && incoming[old] != 0) {
				old++;
			}
			if (old == incoming.length) {
				/*
				 * If that happens, we have automaton that is not acyclic
				 */
				//sortedNodes = null;
				//renumber = null;
				return null;
			}
			renumber[old] = q;
			incoming[old] = -1;
			for (final GenericGraphBox gb : boxes.get(old).transitions) {
				final int destIndex = getBoxIndex((TfstGraphBox) gb, boxes);
				incoming[destIndex]--;
			}
		}
		/*
		 * Finally, we create another array so that sortedNodes[x] will give us
		 * the index of the node whose rank is x
		 */
		List<Integer> sortedNodes = new ArrayList<>(Collections.nCopies(boxes.size(), 0));
		for (int q = 0; q < renumber.length; q++) {
			//sortedNodes[renumber[q]] = q;
			sortedNodes.set(renumber[q], q);
		}

		return sortedNodes;
	}





	public Concatenation factorizeExpressions(Expression expr1, Expression expr2) {
		int size1, size2, i;
		boolean isConcat = false;
		List<Expression> operands1, operands2;
		Concatenation concat = new Concatenation(), tmpConcat1, tmpConcat2;
		Parallelization p = new Parallelization();

		size1 = expr1.getOperandsSize();
		size2 = expr2.getOperandsSize();

		operands1 = expr1.getOperands();
		operands2 = expr2.getOperands();

		//System.out.println("\n\nexp1 ->" + expr1 + "\nexp2" + expr2 + "\n\n");

		/*
		if (expr2 instanceof Concatenation) {
			System.out.println(expr1);
		}
		if (operands2.get(0) instanceof Concatenation) {
			System.out.println(expr2);
		}
		 */


		for (i = 0; i < size1 - 1 && i < size2 - 2; i++) {
			//System.out.println("\n\nCompa ->" + operands1.get(i) + "  --  " + operands2.get(i) + "\n\n");
			
			if (operands1.get(i) == operands2.get(i)) {
				concat.addOperand(operands1.get(i));
			}
			else {
				break;
			}
		}

		if (i != size1 - 1) {
			tmpConcat1 = new Concatenation();
			for (int j = i; j < size1; j++) {
				tmpConcat1.addOperand(operands1.get(j));
			}
			p.addOperand(tmpConcat1);
			isConcat = true;
		}
		else {
			p.addOperand(operands1.get(i));
		}
		
		/*
		if (i > size2 - 2) {
			System.out.println("HAHAHAHA");
			System.out.println("SIZE2 : " + (size2 - 1) + " -  i : " + i + "\n");
		}
		*/
		
		if (i != size2 - 2) {
			tmpConcat2 = new Concatenation();
			for (int j = i; j < size2 - 1; j++) {
				tmpConcat2.addOperand(operands2.get(j));
			}
			p.addOperand(tmpConcat2);
		}
		else {
			p.addOperand(operands2.get(i));
		}


		//System.out.println("PARA1  -> " + p + "\n");
		
		Expression expr = p;
		if (isConcat && p.getOperandsSize() > 2) {
			p.simplify();
		}
		else if (isConcat) {
			/*System.out.println("AAA");
			System.out.println("EXPR1 -> " + expr1 + "\n");
			System.out.println("EXPR2 -> " + expr2 + "\n");*/
			
			expr = p.simplifyAsConcatenation();
		}

		//System.out.println("PARA2  -> " + expr + "\n");
		
		concat.addOperand(expr);
		concat.addOperand(operands2.get(size2 - 1));

		return concat;
	}

	/*
	if (i != size2 - 2) {
			tmpConcat2 = new Concatenation();
			for (j = i; j < size2 - 1; j++) {
				tmpConcat2.addOperand(operands2.get(j));
			}
			p.addOperand(tmpConcat2);
		}
		else {
			p.addOperand(operands2.get(i));
		}


		if (isConcat) {
			p.simplify();
		}


		concat.addOperand(p);
		concat.addOperand(operands2.get(size2 - 1));

		return concat;
	}
	 */




	public String kleeneAlgo() {
		int i, j, k, index;
		Expression expr1, expr2, expr3;

		List<Integer> sortedNodes = topologicalSort(graph);
		//System.out.println(sortedNodes);

		for (i = 0; i < graph.getnBoxes(); i++) {
			List<GenericGraphBox> transitions = graph.getBoxes().get(i).getTransitions();
			for (j = 0; j < transitions.size(); j++) {
				index = graph.getBoxes().indexOf(transitions.get(j));
				map.put(new SimpleEntry<Integer, Integer>(sortedNodes.indexOf(i), sortedNodes.indexOf(index)), new Value(transitions.get(j).getContent()));
			}
		}

		//System.out.println(map + "\n\n");

		for (k = 1; k < sortedNodes.size(); k++) {
			//for (i = 0; i < k; i++) {
			for (j = k + 1; j < sortedNodes.size(); j++) {
				expr1 = map.get(new SimpleEntry<Integer, Integer>(0, k));
				expr2 = map.get(new SimpleEntry<Integer, Integer>(k, j));
				expr3 = map.get(new SimpleEntry<Integer, Integer>(0, j));

/*
				System.out.println("j : " + j + "  -  k : " + k );

				if(expr1 != null) {
					expr1.toString();
					System.out.println("A");
				}

				if(expr2 != null) {
					expr2.toString();
					System.out.println("B");
				}
				
				if (k == 489 && j == 490) {
					System.out.println(expr1);
				}
				
				if(expr3 != null) {
					expr3.toString();
					System.out.println("C");
				}

				System.out.println("Z");
*/
				Concatenation concat = new Concatenation();

				if (expr1 != null && expr2 != null) {

					/*
						Concatenation concat = new Concatenation();
						concat.addOperand(expr1);
						concat.addOperand(expr2);
					 */
					if (expr3 != null) {
						if (expr1 instanceof Value) {
							/*
								System.out.println("EXP1  ->   " +  expr1);
								System.out.println("EXP2  ->   " +  expr2);
								System.out.println("EXP3  ->   " +  expr3);
							 */

							Parallelization p = new Parallelization();
							if (expr3.getOperandsSize() == 2) {
								Expression firstOperand = expr3.getOperands().get(0);
								p.addOperand(expr1);
								p.addOperand(firstOperand);
								concat.addOperand(p);
								concat.addOperand(expr2);
								//System.out.println(i + " - " + j +" - " + k);
								//System.out.println("VAL1 -> " + concat + "\n");
							}
							else {
								Concatenation tmpConcat = new Concatenation();
								tmpConcat.addOperands(expr3, 0, expr3.getOperandsSize() - 1);
								p.addOperand(expr1);
								p.addOperand(tmpConcat);
								concat.addOperand(p);
								concat.addOperand(expr2);
								//System.out.println(i + " - " + j +" - " + k);
								//System.out.println("VAL2 -> " + concat + "\n"); 
							}

							tmpMap.put(new SimpleEntry<Integer, Integer>(0, j), concat);
						}

						else {
							Concatenation tmp = factorizeExpressions(expr1, expr3);
							/*
								System.out.println(i + " - " + j +" - " + k);
								System.out.println("expr1 -> " + expr1); 
								System.out.println("expr3 -> " + expr3); 
								System.out.println("PARA -> " + tmp + "\n"); 
							 */
							tmpMap.put(new SimpleEntry<Integer, Integer>(0, j), factorizeExpressions(expr1, expr3));
						}
					}
					/*
							Parallelization p = new Parallelization();
							p.addOperand(concat);
							p.addOperand(expr3);
							tmpMap.put(new SimpleEntry<Integer, Integer>(i, j), p);
					 */

					//System.out.println(i + "," + j + "," + k + " -> PARALLELE");
					else {
						concat.addOperand(expr1);
						concat.addOperand(expr2);
						//System.out.println(i + " - " + j +" - " + k);
						//System.out.println("CONCAT -> " + concat + "\n");
						tmpMap.put(new SimpleEntry<Integer, Integer>(0, j), concat);
					}
				}
				//System.out.println(i + "," + j + " : 1 -> " + expr1 + " 2 -> " + expr2 + " 3 -> " + expr3 + "\n");
				//}
			}

			Set<Map.Entry<SimpleEntry<Integer, Integer>, Expression>> s = tmpMap.entrySet();
			for (Map.Entry<SimpleEntry<Integer, Integer>, Expression> e : s) {
				map.put(e.getKey(), e.getValue());
			}

			tmpMap.clear();
		}
		
		//System.out.println(graph.getBoxes().get(106).getContent());
		System.out.println(sortedNodes);
		return map.get(new SimpleEntry<Integer, Integer>(0, sortedNodes.size() - 1)).toString();
	}


	@Override
	public String toString() {
		return map.toString();
	}

}
