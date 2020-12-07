package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";

	/**
	 * Populates the vars list with simple variables, and arrays lists with arrays
	 * in the expression. For every variable (simple or array), a SINGLE instance is created
	 * and stored, even if it appears more than once in the expression.
	 * At this time, values for all variables and all array items are set to
	 * zero - they will be loaded from a file in the loadVariableValues method.
	 *
	 * @param expr   The expression
	 * @param vars   The variables array list - already created by the caller
	 * @param arrays The arrays array list - already created by the caller
	 */
	public static void
	makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {

		String name = "";

		for (int i = 0; i < expr.length(); i++) {

			

			char charAtI = expr.charAt(i);

			if (charAtI == ' ' || Character.isDigit(charAtI) || charAtI == '(' || charAtI == ')' || charAtI == '+' || charAtI == '-' || charAtI == '*' || charAtI == '\\' || charAtI == '\t' || charAtI == '[') {
				

				if (i == expr.length() - 1) {
					break;
				}

				
				if (charAtI == '[') {
					
					if (name != "") {
					
						Array arr = new Array(name);
						boolean IsDuplicateArray = false;

						for (int counter = 0; counter < arrays.size(); counter++) {

							if (arrays.get(counter).name.equals(arr.name)) {
								IsDuplicateArray = true;
							}

						}

						if (!IsDuplicateArray) {
							arrays.add(arr);
						}

					}

					name = "";

				} else {
					
					if (name != "") {

						
						Variable var = new Variable(name);
						boolean IsDuplicateVar = false;

						for (int counter = 0; counter < vars.size(); counter++) {

							if (vars.get(counter).name.equals(var.name)) {
								IsDuplicateVar = true;
							}

						}

						if (!IsDuplicateVar) {

							vars.add(var);

						}

					}

					name = "";
				}
			} else {
				
				if (Character.isLetter(charAtI)) {
					name = name + charAtI;
				}
			}
		}

		if (name != "") {
			vars.add(new Variable(name));
		}


		/** COMPLETE THIS METHOD **/
		/** DO NOT create new vars and arrays - they are already created before being sent in
		 ** to this method - you just need to fill them in.
		 **/
	}

	/**
	 * Loads values for variables and arrays in the expression
	 *
	 * @param sc     Scanner for values input
	 * @param vars   The variables array list, previously populated by makeVariableLists
	 * @param arrays The arrays array list - previously populated by makeVariableLists
	 * @throws IOException If there is a problem with the input
	 */
	public static void
	loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays)
			throws IOException {
		while (sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
			int numTokens = st.countTokens();
			String tok = st.nextToken();
			Variable var = new Variable(tok);
			Array arr = new Array(tok);
			int vari = vars.indexOf(var);
			int arri = arrays.indexOf(arr);
			if (vari == -1 && arri == -1) {
				continue;
			}
			int num = Integer.parseInt(st.nextToken());
			if (numTokens == 2) { // scalar symbol
				vars.get(vari).value = num;
			} else { // array symbol
				arr = arrays.get(arri);
				arr.values = new int[num];
				// following are (index,val) pairs
				while (st.hasMoreTokens()) {
					tok = st.nextToken();
					StringTokenizer stt = new StringTokenizer(tok, " (,)");
					int index = Integer.parseInt(stt.nextToken());
					int val = Integer.parseInt(stt.nextToken());
					arr.values[index] = val;
				}
			}
		}
	}

	/**
	 * Evaluates the expression.
	 *
	 * @param vars   The variables array list, with values for all variables in the expression
	 * @param arrays The arrays array list, with values for all array items
	 * @return Result of evaluation
	 */
	public static float evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {

		//WE MUST HAVE 2 SEPERRATE STACKS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		//check for what comes after a
		// an array has functions inside and when you do it a[1+2] = a[3];

		int counter = 0;
		int placement = 0;
		String variable = "";
		boolean isNegative = false;
		if (expr.charAt(0) == '-') {

			isNegative = true;
		}

		String negativeString = "";
		int negativeCounter = 0;

		//get those spaces outta there
		for (int i = 0; i < expr.length(); i++) {

			if (expr.charAt(i) == ' ') {

				expr = expr.substring(0, i) + expr.substring(i + 1);

				return evaluate(expr, vars, arrays);

			}


		}
		//make a loop to put in all the variables and arrays
		for (int i = 0; i < expr.length(); i++) {

			if (Character.isLetter(expr.charAt(i))) {

				variable += expr.charAt(i);

				if (counter == 0) {

					placement = i;
				}

				counter++;


			}
			if (expr.charAt(i) == '*' || expr.charAt(i) == '/' || expr.charAt(i) == '+' || expr.charAt(i) == '-' || expr.charAt(i) == ')' || i == expr.length() - 1) {

				for (int j = 0; j < vars.size(); j++) {
					if (variable.equals(vars.get(j).name)) {

						if (placement != 0) {
							
							if(i == expr.length() - 1) {
								if(expr.charAt(i) == ')'){

									expr = expr.substring(0, placement) + vars.get(j).value + expr.substring(i);

								}else {
									expr = expr.substring(0, placement) + vars.get(j).value + expr.substring(i + 1);
								}
							}else{

								expr = expr.substring(0, placement) + vars.get(j).value + expr.substring(i);

							}
						} else {
							if(variable.length() == expr.length()){
								expr = vars.get(j).value + expr.substring(i+1);

							}else{
								expr = vars.get(j).value + expr.substring(i);

							}


						}
//a*b--b*(-a*-b)
						counter = 0;
						variable = "";
						i = 0;
						break;

					}
				}

			} else if (expr.charAt(i) == '[') {

				//create a method that does calculate on the inside of the array, and then in here will find the array at that space
				// or make a string with everything inside here and call calculate
			
				int bracketCounter = 0;
				for (int j = 0; j < arrays.size(); j++) {
					if (variable.equals(arrays.get(j).name)) {
						String newArrayComp = "";


						for (int k = i + 1; k < expr.length(); k++) {

							if (expr.charAt(k) == ']' && bracketCounter == 0) {

								expr = expr.substring(0, placement) + arrays.get(j).values[(int) Float.parseFloat(calculate(help(newArrayComp, vars, arrays)))] + expr.substring(k + 1);

								return evaluate(expr, vars, arrays);


							} else if(expr.charAt(k) == ']' && bracketCounter > 0) {

								newArrayComp += expr.charAt(k);
								bracketCounter--;

							}else{


								if(expr.charAt(k) == '['){
									newArrayComp += expr.charAt(k);
									bracketCounter++;

								}else{

									newArrayComp += expr.charAt(k);
								}

							}


						}


					}
				}


			}


		}

		//make the n
		for (int i = 0; i < expr.length(); i++) {
			negativeString = "";
			negativeCounter = 0;

			if (expr.charAt(i) == '-') {
				if (isNegative == true) {

					for (int f = i + 1; f < expr.length(); f++) {

						if (Character.isDigit(expr.charAt(f)) || expr.charAt(f) == '.') {

							negativeString += expr.charAt(f);
							negativeCounter++;

						} else {

							break;

						}

					}

					expr = negativeString + "n" + expr.substring(i + negativeCounter + 1);

					isNegative = false;

				} else if (expr.charAt(i - 1) == '+' || expr.charAt(i - 1) == '-' || expr.charAt(i - 1) == '*' || expr.charAt(i - 1) == '/' || expr.charAt(i - 1) == '(') {

					for (int f = i + 1; f < expr.length(); f++) {

						if (Character.isDigit(expr.charAt(f)) || expr.charAt(f) == '.') {

							negativeString += expr.charAt(f);
							negativeCounter++;

						} else {

							break;

						}

					}
					expr = expr.substring(0, i) + negativeString + "n" + expr.substring(i + negativeCounter + 1);
				}


			}

		}
		

		expr = calculate(expr);

		if (expr.charAt(expr.length() - 1) == 'n') {

			expr = "-" + expr.substring(0, expr.length() - 1);

		}

		return Float.parseFloat(expr);


		//make this method recursive, it will help a lot especially when it comes to finding the answer
		//Step 1: Go through and replace all the vars and arrays with actual values within the string
		//Step 2: once you have the string, set up the base case: base case is when the string is just a number
		//Step 3: otherwise, you will have to trace the string and using conditions in pemdas and do the correct operation
		//Step 4: take the section you took out for example (5+2) and replace the entire portion of that string with the value after the operation has been done.
		//in this case, you would replace "(5+2)" with "7"
		//Step 5: as you check, be aware of parathesis and when to multiply
		//Step 6: remember that the use of methods will benefit you in the future.
		//Step 7: the use of the Stack structure will help a lot. make sure to be pushing things into a stack and taking what you need.

		// "(7+(6*(8+4)+(3+4)))"
		//when you find the closing bracket, then push everything before the closing bracket into a new string until you hit the starting bracket.
		//then do the operation and replace like I said before. this can all be done in a method called brackets. make sure you push the answer back into the stack
		//continue your search for other open brackets or closed brackets.
		// after this, we should get a stack like this: (7+(6*12+7
		//next we will have to check the 6*12+7 equation (MAKE SUURE TO REMOVE THE ( BEFORE DOING THE OPERATIONS!)
		//we should first search for multiplication / division, then so the math with addition or subtraction.
		//so we find 6*12, do that operation, get 72 and put it in the stack
		//then put the +7 from the other stack back into the old stack
		//do the addition for 72 + 7 to get 79
		//add 79 to the old stack
		//keep traversing string
		//we hit (7+79 and then a closing bracket
		//take out everything from the stack till the open bracket
		//remove open bracket and closing bracket
		//do the addition to get 86
		//put 86 back into the stack
		//once string == null, make 86 into the expr string. return evaluate and we should get 86 as the answer.


		/** COMPLETE THIS METHOD **/
		// following line just a placeholder for compilation

	}

	private static String calculate(String expr) {// takes the number string for example (1+2)

		int digitCounter = 0;
		boolean parenth = false;

		for (int i = 0; i < expr.length(); i++) {

			if (expr.charAt(i) == '(') {

				parenth = true;
				break;

			}

		}

		if (parenth) {

			for (int i = 0; i < expr.length(); i++) {


				if (expr.charAt(i) == ')') {

					String coolBeans = "";
					int j = i - 1;

					while (expr.charAt(j) != '(') {

						coolBeans += expr.charAt(j);
						j--;

					}
					String reverse = "";
					for (int l = coolBeans.length() - 1; l >= 0; l--) {

						reverse += coolBeans.charAt(l);

					}


					String newString = expr.substring(0, j) + mathBrackets(reverse) + expr.substring(i + 1);

					return calculate(newString);

				} else if (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.') {


					digitCounter++;

				}

			}

			if (digitCounter == expr.length()) {

				return expr;

			}
		} else {


			return mathBrackets(expr);


		}

		return null;
	}

	private static String mathBrackets(String expr) {//doing any expression like 6+8*2/2

		// float to string and string to float
		//float number = -895.25f;
		//String numberAsString = Float.toString(number);

		float firstNum = 0;
		String firstNumString = "";
		float secondNum = 0;
		String secondNumString = "";

		//these are for counting the number of digits are in each number..helps with where to start the substring
		int firstCounter = 0;
		int secondCounter = 0;

		int operatorCounter = 0;

		boolean firstIsNegative = false;
		boolean secondIsNegative = false;
//
		for (int i = 0; i < expr.length(); i++) { // this one will look for multiplication or division


			if (expr.charAt(i) == '+' || expr.charAt(i) == '-') {

				operatorCounter++;

			}

			if (expr.charAt(i) == '*') {

				float product = 0;
				if (operatorCounter != 0) {
					int n = i - 1;
					while (expr.charAt(n) != '+' && expr.charAt(n) != '-') {
						if (expr.charAt(n) == 'n') {

							firstIsNegative = true;

						} else {

							firstNumString += expr.charAt(n);

						}
						n--;

					}

				} else {

					for (int n = i - 1; n >= 0; n--) {

						if (expr.charAt(n) == 'n') {

							firstIsNegative = true;

						} else {

							firstNumString += expr.charAt(n);

						}

					}

				}
				
				//reverse firstNumString
				String reverse = "";
				if (firstIsNegative == true) {

					reverse += '-';
					firstCounter++;
				}

				if (firstNumString.length() >= 2) {

					for (int l = firstNumString.length() - 1; l >= 0; l--) {

						reverse += firstNumString.charAt(l);
						firstCounter++;

					}
					firstNum = Float.parseFloat(reverse);

				} else {

					if (firstIsNegative == true) {

						firstCounter++;
						firstNum = -1 * Float.parseFloat(firstNumString);

					} else {

						firstCounter++;
						firstNum = Float.parseFloat(firstNumString);
					}
				}

				for (int k = i + 1; k < expr.length(); k++) {

					if (expr.charAt(k) == '.' || Character.isDigit(expr.charAt(k)) || expr.charAt(k) == 'n') {

						if (expr.charAt(k) == 'n') {

							secondIsNegative = true;

						} else {

							secondNumString += expr.charAt(k);

						}
						secondCounter++;

					} else {

						break;

					}


				}
				if (secondIsNegative == true) {

					secondNumString = "-" + secondNumString;

				}

				secondNum = Float.parseFloat(secondNumString);
				product = firstNum * secondNum;

				if ((firstIsNegative && !secondIsNegative) || (!firstIsNegative && secondIsNegative)) {

					product = product * -1;

					try {
						expr = expr.substring(0, i - firstCounter) + product + "n" + expr.substring(i + secondCounter + 1);
					} catch (Exception e) {
						if (i - firstCounter == 0 && !(i + secondCounter + 1 > expr.length() - 1)) {

							expr = expr.substring(0, i - firstCounter) + product + "n";

						} else if (i + secondCounter + 1 > expr.length() - 1 && !(i - firstCounter == 0)) {

							expr = product + "n" + expr.substring(i + secondCounter + 1);

						} else {

							expr = Float.toString(product);

						}

					}

				} else {

					try {
						expr = expr.substring(0, i - firstCounter) + product + expr.substring(i + secondCounter + 1);
					} catch (Exception e) {
						if (i - firstCounter == 0 && !(i + secondCounter + 1 > expr.length() - 1)) {

							expr = expr.substring(0, i - firstCounter) + product;

						} else if (i + secondCounter + 1 > expr.length() - 1 && !(i - firstCounter == 0)) {

							expr = product + expr.substring(i + secondCounter + 1);

						} else {

							expr = Float.toString(product);

						}

					}
				}
				return mathBrackets(expr);

			} else if (expr.charAt(i) == '/') {

				float quotient = 0;

				if (operatorCounter != 0) {
					int n = i - 1;
					while (expr.charAt(n) != '+' && expr.charAt(n) != '-') {
						if (expr.charAt(n) == 'n') {

							firstIsNegative = true;

						} else {

							firstNumString += expr.charAt(n);

						}
						n--;

					}

				} else {

					for (int n = i - 1; n >= 0; n--) {

						if (expr.charAt(n) == 'n') {

							firstIsNegative = true;

						} else {

							firstNumString += expr.charAt(n);

						}

					}

				}
				
				//reverse firstNumString
				String reverse = "";
				if (firstIsNegative == true) {

					reverse += '-';
					firstCounter++;
				}

				if (firstNumString.length() >= 2) {

					for (int l = firstNumString.length() - 1; l >= 0; l--) {

						reverse += firstNumString.charAt(l);
						firstCounter++;

					}
					firstNum = Float.parseFloat(reverse);

				} else {

					if (firstIsNegative == true) {

						firstCounter++;
						firstNum = -1 * Float.parseFloat(firstNumString);

					} else {

						firstCounter++;
						firstNum = Float.parseFloat(firstNumString);
					}
				}

				for (int k = i + 1; k < expr.length(); k++) {

					if (expr.charAt(k) == '.' || Character.isDigit(expr.charAt(k)) || expr.charAt(k) == 'n') {

						if (expr.charAt(k) == 'n') {

							secondIsNegative = true;

						} else {

							secondNumString += expr.charAt(k);

						}
						secondCounter++;

					} else {

						break;

					}


				}
				if (secondIsNegative == true) {

					secondNumString = "-" + secondNumString;

				}

				secondNum = Float.parseFloat(secondNumString);
				quotient = firstNum / secondNum;

				if ((firstIsNegative && !secondIsNegative) || (!firstIsNegative && secondIsNegative)) {

					quotient = quotient * -1;

					try {
						expr = expr.substring(0, i - firstCounter) + quotient + "n" + expr.substring(i + secondCounter + 1);
					} catch (Exception e) {
						if (i - firstCounter == 0 && !(i + secondCounter + 1 > expr.length() - 1)) {

							expr = expr.substring(0, i - firstCounter) + quotient + "n";

						} else if (i + secondCounter + 1 > expr.length() - 1 && !(i - firstCounter == 0)) {

							expr = quotient + "n" + expr.substring(i + secondCounter + 1);

						} else {

							expr = Float.toString(quotient);

						}

					}

				} else {

					try {
						expr = expr.substring(0, i - firstCounter) + quotient + expr.substring(i + secondCounter + 1);
					} catch (Exception e) {
						if (i - firstCounter == 0 && !(i + secondCounter + 1 > expr.length() - 1)) {

							expr = expr.substring(0, i - firstCounter) + quotient;

						} else if (i + secondCounter + 1 > expr.length() - 1 && !(i - firstCounter == 0)) {

							expr = quotient + expr.substring(i + secondCounter + 1);

						} else {

							expr = Float.toString(quotient);

						}

					}
				}
				return mathBrackets(expr);

			}
		}

		firstNumString = "";
		secondNumString = "";
		firstCounter = 0;
		secondCounter = 0;

		firstIsNegative = false;
		secondIsNegative = false;
		for (int i = 0; i < expr.length(); i++) {// this one will look for addition or subtraction

			if (expr.charAt(i) == '+') {

				float sum = 0;

				if (firstIsNegative == true) {

					firstNum = -1 * Float.parseFloat(firstNumString);

				} else {

					firstNum = Float.parseFloat(firstNumString);

				}

				for (int k = i + 1; k < expr.length(); k++) {

					if (expr.charAt(k) == '.' || Character.isDigit(expr.charAt(k)) || expr.charAt(k) == 'n') {

						if (expr.charAt(k) == 'n') {

							secondIsNegative = true;

						} else {

							secondNumString += expr.charAt(k);

						}
						secondCounter++;

					} else {

						break;

					}


				}
				if (secondIsNegative == true) {

					secondNumString = "-" + secondNumString;

				}

				

				secondNum = Float.parseFloat(secondNumString);
				sum = firstNum + secondNum;

				if (sum < 0) {

					sum = sum * -1;

					try {
						expr = expr.substring(0, i - firstCounter) + sum + "n" + expr.substring(i + secondCounter + 1);
					} catch (Exception e) {
						if (i - firstCounter == 0 && !(i + secondCounter + 1 > expr.length() - 1)) {

							expr = expr.substring(0, i - firstCounter) + sum + "n";

						} else if (i + secondCounter + 1 > expr.length() - 1 && !(i - firstCounter == 0)) {

							expr = sum + "n" + expr.substring(i + secondCounter + 1);

						} else {

							expr = Float.toString(sum);

						}

					}

				} else {

					try {
						expr = expr.substring(0, i - firstCounter) + sum + expr.substring(i + secondCounter + 1);
					} catch (Exception e) {
						if (i - firstCounter == 0 && !(i + secondCounter + 1 > expr.length() - 1)) {

							expr = expr.substring(0, i - firstCounter) + sum;

						} else if (i + secondCounter + 1 > expr.length() - 1 && !(i - firstCounter == 0)) {

							expr = sum + expr.substring(i + secondCounter + 1);

						} else {

							expr = Float.toString(sum);

						}

					}
				}

				return mathBrackets(expr);

			} else if (expr.charAt(i) == '-') {

				float difference = 0;
				if (firstIsNegative == true) {

					firstNum = -1 * Float.parseFloat(firstNumString);

				} else {

					firstNum = Float.parseFloat(firstNumString);

				}

				for (int k = i + 1; k < expr.length(); k++) {

					if (expr.charAt(k) == '.' || Character.isDigit(expr.charAt(k)) || expr.charAt(k) == 'n') {

						if (expr.charAt(k) == 'n') {

							secondIsNegative = true;

						} else {

							secondNumString += expr.charAt(k);

						}
						secondCounter++;

					} else {

						break;

					}


				}
				if (secondIsNegative == true) {

					secondNumString = "-" + secondNumString;

				}

				

				secondNum = Float.parseFloat(secondNumString);
				difference = firstNum - secondNum;

				if (difference < 0) {

					difference = difference * -1;

					try {
						expr = expr.substring(0, i - firstCounter) + difference + "n" + expr.substring(i + secondCounter + 1);
					} catch (Exception e) {
						if (i - firstCounter == 0 && !(i + secondCounter + 1 > expr.length() - 1)) {

							expr = expr.substring(0, i - firstCounter) + difference + "n";

						} else if (i + secondCounter + 1 > expr.length() - 1 && !(i - firstCounter == 0)) {

							expr = difference + "n" + expr.substring(i + secondCounter + 1);

						} else {

							expr = Float.toString(difference);

						}

					}

				} else {

					try {
						expr = expr.substring(0, i - firstCounter) + difference + expr.substring(i + secondCounter + 1);
					} catch (Exception e) {
						if (i - firstCounter == 0 && !(i + secondCounter + 1 > expr.length() - 1)) {

							expr = expr.substring(0, i - firstCounter) + difference;

						} else if (i + secondCounter + 1 > expr.length() - 1 && !(i - firstCounter == 0)) {

							expr = difference + expr.substring(i + secondCounter + 1);

						} else {

							expr = Float.toString(difference);

						}

					}
				}

				return mathBrackets(expr);

			} else if (expr.charAt(i) == '.' || Character.isDigit(expr.charAt(i)) || expr.charAt(i) == 'n') { //ADD THIS TO ADD AND SUB
				if (expr.charAt(i) == 'n') {

					firstIsNegative = true;

				} else {

					firstNumString += expr.charAt(i);

				}
				firstCounter++;
			}

		}

		return expr;

	}

	private static String help(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) { //ive got to make a case for arrays

		int counter = 0;
		int placement = 0;
		String variable = "";


		for (int i = 0; i < expr.length(); i++) {

			if (Character.isLetter(expr.charAt(i))) {

				variable += expr.charAt(i);

				if (counter == 0) {

					placement = i;
				}

				counter++;


			}
			if (expr.charAt(i) == '*' || expr.charAt(i) == '/' || expr.charAt(i) == '+' || expr.charAt(i) == '-' || expr.charAt(i) == ')' || i == expr.length() - 1) {

				for (int j = 0; j < vars.size(); j++) {
					if (variable.equals(vars.get(j).name)) {

						if (placement != 0) {
							
							if (i == expr.length() - 1) {
								if(expr.charAt(i) == ')'){

									expr = expr.substring(0, placement) + vars.get(j).value + expr.substring(i);

								}else {
									expr = expr.substring(0, placement) + vars.get(j).value + expr.substring(i + 1);
								}
							} else {

								expr = expr.substring(0, placement) + vars.get(j).value + expr.substring(i);

							}
						} else {

							expr = vars.get(j).value + expr.substring(i);

						}
//a*b--b*(-a*-b)
						counter = 0;
						variable = "";
						i = 0;
						break;

					}
				}

			} else if (expr.charAt(i) == '[') {

				//create a method that does calculate on the inside of the array, and then in here will find the array at that space
				// or make a string with everything inside here and call calculate
				int bracketCounter = 0;
				for (int j = 0; j < arrays.size(); j++) {
					if (variable.equals(arrays.get(j).name)) {
						String newArrayComp = "";


						for (int k = i + 1; k < expr.length(); k++) {

							if (expr.charAt(k) == ']' && bracketCounter == 0) {

								expr = expr.substring(0, placement) + arrays.get(j).values[(int) Float.parseFloat(calculate(help(newArrayComp, vars, arrays)))] + expr.substring(k + 1);

								break;


							} else if(expr.charAt(k) == ']' && bracketCounter > 0) {

								newArrayComp += expr.charAt(k);
								bracketCounter--;

							}else{


								if(expr.charAt(k) == '['){
									newArrayComp += expr.charAt(k);
									bracketCounter++;

								}else{

									newArrayComp += expr.charAt(k);
								}

							}


						}


					}
				}


			}
		}
		return expr;
	}
}