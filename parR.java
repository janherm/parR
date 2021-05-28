/**
 * Calculates the best target value (user input), using two/three parallel E24 resistors 
 * (possibly with an extra series resistor), or with series resistors (as much as needed).
 * 
 * Version 1.0, 06-05-2021
 * Version 2.0, 28-05-2021: Series resistors added (thanks for Rod Elliott's feedback)
 * (c) 2021 JanHerman Verpoorten
 */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;
import java.text.DecimalFormat;
import java.math.RoundingMode;
import java.math.BigDecimal;


public class parR {
  public static void main(String[] args)
   {  JFrame frame = new parRFrame();
      frame.show();
   }
}
	 
	 
class MatrixResult {
 	int i=0, j=0;
 	String s="";
 	double d=0;
}

/* 
* Main class parRFrame
*	
*/

class parRFrame extends JFrame { 
	double[] E24 = {1.0,1.1,1.2,1.3,1.5,1.6,1.8,2.0,2.2,2.4,2.7,3.0,3.3,3.6,3.9,4.3,4.7,5.1,5.6,6.2,6.8,7.5,8.2,9.1};
	int[] mult = {1,10,100,1000,10000,100000,1000000,10000000};
	JPanel panel;
	String popupmsg;
	String popupmsg2;  //possibly used in calculateSer, first approach

	/**
	Looks for a value in the 2D matrix, containing E24 sums.
	*/
	public boolean isInMatrix(double[][] matrix, double val) {
		val = round(val,1);
		//System.out.println("starting isInMatrix with val "+val);
		for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[i].length; j++) {
      	if (matrix[i][j]== val) {
      		return true;
      	}
      }
    }  
    return false;	
	}

	/**
	Method returns a formatted string, common for the presentation of resistor values:
	containing K and M for Kilo (1000) and Mega (1000000) resp., and using this letters 
	as a delimitor.<br>
	For instance 4K7 means 4700 Ohms.
	*/
	public String formatR(double arg) {
		int m=0;
		double d =arg;
		while (d>=10){
			d= d/10;
			m +=1;
		}
		String s = Double.toString(d);
		//System.out.println("s="+s);
		String sub = s.substring(0,s.indexOf('.')).concat(s.substring(s.indexOf('.')+1,3));
		//System.out.println("sub="+sub);
		switch (m) {
			case 0: return s.concat("\u03A9");
			case 1: return sub.concat("\u03A9");
			case 2: return sub.concat("0\u03A9");
			case 3: return s.substring(0,s.indexOf('.')).concat("K").concat(s.substring(s.indexOf('.')+1,3));
			case 4: return sub.concat("K");
			case 5: return sub.concat("0K");
			case 6: return s.substring(0,s.indexOf('.')).concat("M").concat(s.substring(s.indexOf('.')+1,3));
			case 7: return sub.concat("M");
			default:
			return Double.toString(arg);
		}
	}
	
	/**
	* Looks for resistor pattern #.# in the E24 range
	*/
	public boolean isInE24(double val, double[] E24) { 
		for (int i=0;i<E24.length;i++){
			if (E24[i]==val) return true;
		}
		return false;
	}

	/**
		Calculates the resistor value, needed to improve the end result,
	  achieved with parallel resistors.<br>
	  In most cases, it results in a small value, only to decrease the already minor error.
	*/	
	public double calcSeriesR(double target, double Rtarget) {
		double[] serieR={0,0};
		double dif=target-Rtarget; 
		int m=0;
		while (dif>10){
			dif= dif/10;
			m +=1;
			}
		for (int i=0;i<E24.length;i++) {
			if (E24[i]<=dif) {
				serieR[0]=E24[i]*mult[m];
				//look for next value in E24 range
				if (i<E24.length-1) {
					serieR[1]=E24[i+1]*mult[m];
				}else if (m<mult.length-1){
					serieR[1]=E24[0]*mult[m+1];
				}else serieR[1]=Double.NaN;
			}else
			break;
		}
		double err0=(Math.abs(target-(Rtarget+serieR[0])) / target)*100;
		double err1;
		if (serieR[1] !=Double.NaN)
			err1=(Math.abs(target-(Rtarget+serieR[1])) / target)*100;
		else err1=999;
		if (err0<err1) 
			return serieR[0];
		else
			return serieR[1];
	}	
	
	
	/**
	During the calculation, String 'popupmsg' is build up. When ready, this method presents
	a popup window, rendering this string with all calculation results.	<br>
	Is called by method calculate(Double target).
	*/
	public void popup(String msg, double target) {
		targetInput.setText("");
		//log.setText("Done");
		
		javax.swing.border.Border empty = javax.swing.BorderFactory.createEmptyBorder(10,10,10,10);
		String ms = "All RESULTS FOR TARGET "+target+" OHM\n\nWITH PARALLEL RESISTORS:\n";
		ms = ms.concat(msg);
		JTextArea jta = new JTextArea(ms, 15,40);
		jta.setLineWrap(true);
		jta.setWrapStyleWord(true);
		jta.setFont(new Font("Verdana",Font.PLAIN,12));
		jta.setBorder(empty);

		JScrollPane scrollPane = new JScrollPane(jta);

		JOptionPane.showMessageDialog(panel, scrollPane,"CALCULATION",JOptionPane.PLAIN_MESSAGE);
	}
	
	/**
	* Sorts an array from high to low.
	*
	*/
	public double[] sortHiLo(double[] values) {
	 	Arrays.sort(values);
		double arr[] = new double[values.length];
	 	int j=0;
	 	for (int i=values.length-1;i>=0;i--){
	 		arr[j]=values[i];
	 		j++;
	 	}
		return arr;
	}
	
	public Vector inMatrix(double val, double[][] matrix){
		DecimalFormat df0 = new DecimalFormat("#");
		DecimalFormat df1 = new DecimalFormat("#.#");
		df0.setRoundingMode(RoundingMode.CEILING);	
		df1.setRoundingMode(RoundingMode.CEILING);	
		double[] E24 = {1.0,1.1,1.2,1.3,1.5,1.6,1.8,2.0,2.2,2.4,2.7,3.0,3.3,3.6,3.9,4.3,4.7,5.1,5.6,6.2,6.8,7.5,8.2,9.1};
		double d=val; 
		int muld=1;
		while (d>18.2){
			d=d/10;
			muld=muld*10;;
		}
		d=round(d,1);//this creates always a two resistor solution, but with a certain error
		//System.out.println("looking in matrix for "+d);
		Vector mrs = new Vector(); 
		String str;
		MatrixResult m;
		for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[i].length; j++) {
      	if (matrix[i][j] == d) {
      		if (muld>1)
      			str = formatR(E24[i]*muld)+" + "+formatR(E24[j]*muld);
      			else
      			str = formatR(E24[i]*muld)+" + "+formatR(E24[j]*muld);
      			//str = df1.format(E24[i]*muld)+" + "+df1.format(E24[j]*muld);
      		//System.out.println(str);
      		m= new MatrixResult();
      		m.i=i;
      		m.j=j;
      		m.s=str;
      		m.d=(E24[i]*muld)+(E24[j]*muld);
      		mrs.addElement(m);
      	}
      }
    }  
		return mrs;		
	}


	/** ===================================================================================
		Main method. Calls both methods for calculating the parallel and series resistors
		and presents the results. 
		========================================================================================
	*/
	public void calculate (Double target) {
		popupmsg="";
		popupmsg2=""; //possibly used in calculateSer in the 1st approach
		
		if (target <= 0) {
			JOptionPane.showMessageDialog(panel, "Negative values are invalid","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (target > mult[mult.length-1]) {
			JOptionPane.showMessageDialog(panel, "Target value "+target+" is too big","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		calculatePar(target);
		calculateSer(target);
		popup(popupmsg,target);
	}
	
	/**
	Tries to achieve the target value by using two or three resistors in parallel. 
	Brute force approach: all possible combinations with all possible E24 values 
	of 2 resp. 3 resistors are examined, and the very best is presented.<br> 
	In some cases, an acceptable end result cannot be achieved. If so, the target value is 
	lowered in one Ohm steps, until -5% of the target value. <br>
	Although the final outcome in most cases is very precise, at any error >0  
	an extra series resistor is calculated, further lowering the error factor. 
	<br>See method calcSeriesR(target, Rtarget).
	*/
	public void calculatePar(Double target) {
		double Rtarget=0, Rres=0, Ra=0, Rb=0, Rc=0, R1=0, R2=0, R3=0, diff=0;
		double err, d;
		double delta=999999999.99;//start val;
		
		Rtarget=target; //Rtarget is the remaining value in progress. Ready if <1.
		DecimalFormat df = new DecimalFormat("#.##");
		DecimalFormat df0 = new DecimalFormat("#");
		DecimalFormat df1 = new DecimalFormat("#.#");
		DecimalFormat df2 = new DecimalFormat("#.##");
		df0.setRoundingMode(RoundingMode.CEILING);	
		df1.setRoundingMode(RoundingMode.CEILING);	
		df2.setRoundingMode(RoundingMode.CEILING);	

		df.setRoundingMode(RoundingMode.CEILING);
		
	do{
		//already an E24 value?
		double r=Rtarget;
		int m=0;
		while (r>10){
			r= r/10;
			m +=1;
			}
		for (int i=0;i<E24.length;i++) {
			if (E24[i]==r) {
				err = (Math.abs(target-Rtarget) / target)*100;
				popupmsg=("   No parallel resistors needed: "+formatR(Rtarget)+" is a E24 value; error is "+df.format(err)+"%\n" );
				if (err !=0) { //advise an extra series R
					double sr=calcSeriesR(target, Rtarget);
					popupmsg = popupmsg.concat("   Optional: Add ").concat(formatR(sr)+" ").concat("in series, ");
					err=(Math.abs(target-(Rtarget+sr)) / target)*100;
					popupmsg=popupmsg.concat("creating a final error value "+df.format(err)+"%.\n" );
				}
				//popup(popupmsg,target);
				return;
			}
		}
		/*first try with two resistors:
		*/
		double res;
		R2=0;
		R3=0;
		for (int i=0;i<E24.length;i++){
			int x =m;
			do{
				R1 = mult[x] * E24[i]; 
				for (int j=0;j<E24.length;j++){
					int y = m;
					do{
						R2 =  mult[y] * E24[j]; 
						res = 1 /((1/R1)+(1/R2)); 
						if (res ==Rtarget) { //early ready
							err = (Math.abs(target-res) / target)*100;
							popupmsg=("   Ra="+formatR(R1) +", Rb="+formatR(R2)+"\n   Result is  "+res+" ; error is "+df.format(err)+"%\n");
							if (err !=0) {
								double sr=calcSeriesR(target, Rtarget);
								popupmsg = popupmsg.concat("   Optional: Add ").concat(formatR(sr)+" ").concat("in series,");
								err=(Math.abs(target-(Rtarget+sr)) / target)*100;
								popupmsg=popupmsg.concat(" creating a final error value "+df.format(err)+"%,\n" );
							}
							//popup(popupmsg,target);
							return;
						} else {
							diff = Math.abs(Rtarget-Rres); 
							if (diff < delta) { //save values
								delta=diff;
								Rres=res;
								Ra=R1;
								Rb=R2;
								Rc=0;	//System.out.println("delta="+Math.round(delta)+ ", Ra="+Ra+", Rb="+Rb);					
							}
						}
						y+=1;
					}while (y<mult.length);
				}
				x+=1;
			}while (x<mult.length);
		}

		//next try with three resistors
		for (int i=0;i<E24.length;i++){ 
			int x =m;
			do{
				R1 = mult[x] * E24[i]; 
				for (int j=0;j<E24.length;j++){ 
					int y = m;
					do{
						R2 =  mult[y] * E24[j]; 
						for (int k=0;k<E24.length;k++){ 
							int z=m;
							do{
								R3= mult[z] * E24[k]; 
								res = 1 /((1/R1)+(1/R2)+(1/R3));
								if (res ==Rtarget) { //early ready
									err = (Math.abs(target-res) / target)*100;
									popupmsg=("   Ra="+formatR(R1) +", Rb="+formatR(R2)+", Rc="+formatR(R3)+"\n   Result is  "+res+" ; error is "+df.format(err)+"%\n");
									if (err !=0) {
										double sr=calcSeriesR(target, Rtarget);
										popupmsg = popupmsg.concat("   Optional: Add ").concat(formatR(sr)+" ").concat("in series,");
										err=(Math.abs(target-(Rtarget+sr)) / target)*100;
										popupmsg=popupmsg.concat(" creating a final error value "+df.format(err)+"%,\n" );
									}
									//popup(popupmsg,target);
									return;
								} else {
									diff = Math.abs(Rtarget-Rres); 
									if (diff < delta) { //save values
										delta=diff;
										Rres=res;
										Ra=R1;
										Rb=R2;
										Rc=R3;
									}
								}
								z+=1;
							}while (z<mult.length);
						}
						y+=1;
					}while (y<mult.length);
				}
				x+=1;
			}while (x<mult.length);
		}
		
		Rres=Math.round(Rres);
			err = (Math.abs(Rtarget-Rres) / Rtarget)*100;
			d = ((target-Rtarget) / target) *100;
			Rtarget-=1;
} while ((err>10.0) && (d<5.0));
		
	err = (Math.abs(Rtarget-Rres) / Rtarget)*100;
	if ((err !=0) && (err <=5.0)) {
		if (Rc==0)
			popupmsg=("\n   Best found two values are "+formatR(Ra)+" and "+formatR(Rb)+"\n\n");
		else 
			popupmsg="\n   Best found three values are "+formatR(Ra)+", "+formatR(Rb)+" and "+formatR(Rc)+"\n";

		popupmsg=popupmsg.concat("   Last attempted target value is "+Rtarget+", achieved is "+Rres+".\n   Difference: "+Math.abs(Rtarget-Rres)+" Ohm\n");
		popupmsg=popupmsg.concat("   Error is "+ df2.format(err) + "%\n");
		double sr=calcSeriesR(target, Rres);
		popupmsg = popupmsg.concat("   Optional: Add ").concat(formatR(sr)+" ").concat("in series,");
		err=(Math.abs(target-(Rtarget+sr)) / target)*100;				
		popupmsg=popupmsg.concat(" creating a final error value "+df2.format(err)+"%,\n" );
		popupmsg=popupmsg.concat("   Or get your result with series resistors (below).\n" );
		} else {
			popupmsg=popupmsg.concat("   Last attempted target value is "+Rtarget+"\n");
			popupmsg=popupmsg.concat("   No parallel solution, try an acceptable higher target\n" );
		}
	
	}//endof calculatePar
	
	//============================================================================================
	

	//add a found value to the array, containing all found values
	public double[] addVal(double arr[], double val) {
		int len =arr.length;
		double array[] = new double[len+1];
		for (int i=0;i<len;i++) array[i] = arr[i];
		array[len]=val;
    return array;
    }

	//calculates the sum of all values in the array, containing all found values
	public double sum(double[] arr) {
		double d=0;
		for (int i=0;i<arr.length;i++) {
			d += arr[i];
		}
		return d;
	}

	public double round(double value, int places) {
   	if (places < 0) throw new IllegalArgumentException();
   	BigDecimal bd = BigDecimal.valueOf(value);
   	bd = bd.setScale(places, RoundingMode.HALF_UP);
   	return bd.doubleValue();
	}
	
	public double truncateDecimal(double x,int numberofDecimals){
  	if ( x > 0) {
      return (new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR).doubleValue());
  	} else {
      return (new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING).doubleValue());
  	}
	}
	
	
	
	/**
	* Method uses three algorithmes and results in three arrays of E24 conformant values
	*
	*/	
	public void calculateSer(Double target) {
		double Rtarget=target; //Rtarget is the remaining value in progress. Ready if <1.
		double err;
		double[] values1 ={};
		double[] values2 ={};
		double[] values3 ={};
		boolean ready=false;
		
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);
		DecimalFormat df0 = new DecimalFormat("#");
		DecimalFormat df1 = new DecimalFormat("#.#");
		DecimalFormat df2 = new DecimalFormat("#.##");
		df0.setRoundingMode(RoundingMode.CEILING);	
		df1.setRoundingMode(RoundingMode.CEILING);	
		df2.setRoundingMode(RoundingMode.CEILING);	
		
		/* =================================FIRST APPROACH====================================
			Creates an acceptable two resistor result (some error).
			Approach: If target value not in a 2D matrix of E24 sums, then subtract the highest 
			possible E424 value in a loop, until error <0.5%.
			Either string popupmsg2, or values1 is created.
		  ===================================================================================== 
		  */ 
		Rtarget = target;
		//int round = 1;
		int muld3, muld2; //multiplication factor
		ready=false;
		double d2,d3;
		popupmsg2="";

		//Create matrix with sums of all E24 values
		//Absent sums: below 10 only 9.6, between 10 and 18.2 37 values (out of 82) 
		double[][] matrix = new double[E24.length][E24.length];
		for (int i = 0; i < E24.length; i++) {
      for (int j = 0; j < E24.length; j++) {
      	matrix[i][j] = round((E24[i]+E24[j]),1);
      }
    }
		//first look in matrix with pattern ##.# or modulus zero
		double d=target;
		while (d>18.2){
			d=d/10;
		}
		if ( (target%10==0) || isInMatrix(matrix, d)){
			//look in matrix for fast two resistor / some error solution
			Vector mrs = inMatrix(target, matrix);
			Vector skiplist = new Vector();
			if (mrs.size() > 0){
				skiplist.clear(); 
				int x,y;
				for (int i=0;i<mrs.size()-1;i++){ //remove double results like x+y == y+x
					x=((MatrixResult)mrs.elementAt(i)).i;
					y=((MatrixResult)mrs.elementAt(i)).j;
					for (int j=i+1;j<mrs.size();j++){
						if(x+y==((MatrixResult)mrs.elementAt(j)).i+((MatrixResult)mrs.elementAt(j)).j){
							if (!(skiplist.contains(new Integer(j)))) {
								skiplist.addElement(new Integer(j));
							}
						}
					}
				}
				//get result value
				double res= round(((MatrixResult)mrs.elementAt(0)).d, 2);
				popupmsg2 =Double.toString(res).concat("=" );
				int out = mrs.size()-skiplist.size(); 
				for (int i=0;i<mrs.size();i++){
					//als niet in skiplist dan voeg toe
					if (!(skiplist.contains(new Integer(i)))) {	
						popupmsg2 =popupmsg2.concat("(").concat(((MatrixResult)mrs.elementAt(i)).s).concat(")");
						out--; 
					  if (out > 0) popupmsg2 = popupmsg2.concat(" or ");
					}
				}
				double e = (Math.abs(target-res) / target)*100;
				popupmsg2=popupmsg2.concat("\n      Error is ").concat(df2.format(e)).concat("%");;
			}

		} else { // not in matrix or modulus 0, subtract biggest possible value
			do {	
				d2=Rtarget; 
				muld2=1;
				while (d2>=10){
					d2=d2/10;
					muld2=muld2*10;;
				}
				d2 = truncateDecimal(d2,1); //#.#
				if (isInE24(d2, E24)){
					values1 =addVal(values1,d2*muld2); 
				} else {
	// subtract highest E24 value below Rtarget	
					d=0;
					if ((d2==1.0)&& (muld2>1)) {
						values1 = addVal(values1, 9.1*muld2/10); 
					} else {
						for (int i=0;i<E24.length;i++) {
							if ((E24[i]< d2)){
								d = (E24[i]);
							}
						}
						if (d!=0) values1 = addVal(values1, d*muld2); 
					}
				}
				Rtarget = target - sum(values1);
				err = (Math.abs(target-sum(values1)) / target)*100;
				ready = ((Rtarget < 1) || (err<0.5));	
			} while (!ready);
			
			if (values1.length==0) 
				popupmsg2="   No sufficient result with first algorithm.";
	} //endof else

		/* =========================SECOND APPROACH============================= 
			Create a TreeMap(K,V) with E24 values, summed up to 100.1 (9.1+91).
			Keys are the calculated sums, Values are the x+y expressions
			The difference with the matrix used in step 1: 
			- No double sums, only the last found is stored;
			- Sums up to 100.1, beside x+y, also x+10y is calculated.
			Just as with the matrix, the downside is that the map isn't complete: 
			it only contains 545 unique sums out of 998.
			However, it can speed up any search and may present values different
			from the matrix, which may help to construct a different solution.
			The do..while loop consists of:
			1. The very first step is looking for an existing E24 value, 
				 as is done also in the two other approaches.
			2. Next, first the treemap is inspected. 
			3. If that fails, look for the biggest value to subtract that makes it
			   possible to find the remaining values in TreeMap. 
			======================================================================
		*/
		
		TreeMap<Double, String> tm= new TreeMap<Double, String>();
		Double D;
		String S;
		for (int i = 0; i < E24.length; i++) {
      for (int j = 0; j < E24.length; j++) {
      	S=new Double(E24[i]).toString().concat("+").concat(new Double(E24[j]).toString());
      	d=E24[i]+E24[j];
      	d=round(d,1);
      	D=new Double(d);
      	tm.put(D,S);
      	S=new Double(E24[i]).toString().concat("+").concat(new Double(10*E24[j]).toString());
      	d=E24[i]+(10*E24[j]);
      	d=round(d,1);
      	D=new Double(d);
      	tm.put(D,S);
  	  }	
	  }
	  
	  ready=false; 
		Rtarget =target;
//----0. create new patterns
	do {
		d3=Rtarget; //d2 and d3 for patterns #.# and ##.#
		muld3=1;
		while (d3>=100){
			d3=d3/10;
			muld3=muld3*10;
		}
		d3 = truncateDecimal(d3,1); //##.# or #.#
		if (d3 >=10) {
			d2 = d3/10;
			muld2=muld3*10;
		}	else{
			d2=d3;
			muld2=muld3;
		}
		d2 = truncateDecimal(d2,1); //#.#
		String s="";
	//STEP 1. First look in the TreeMap with pattern #.# and ##.# 
			if (tm.get(new Double(d3))!=null){ 
					s = tm.get(new Double(d3));
					//parse s
					double db1 = muld3*(new Double(s.substring(0, s.indexOf('+'))).doubleValue());
					double db2 = muld3*(new Double(s.substring(s.indexOf('+')+1)).doubleValue());
					values2 = addVal(values2, db1);
					values2 = addVal(values2, db2);
					Rtarget = target - sum(values2);
				Rtarget = target - sum(values2);
				err = (Math.abs(target-sum(values2)) / target)*100;
				ready = ((Rtarget < 1) || (err<0.5));	
			} else if (tm.get(new Double(d2))!=null){ 
					s = tm.get(new Double(d2));
					//parse s
					double db1 = muld2*(new Double(s.substring(0, s.indexOf('+'))).doubleValue());
					double db2 = muld2*(new Double(s.substring(s.indexOf('+')+1)).doubleValue());
					values2 = addVal(values2, db1);
					values2 = addVal(values2, db2);
					Rtarget = target - sum(values2);
				Rtarget = target - sum(values2);
				err = (Math.abs(target-sum(values2)) / target)*100;
				ready = ((Rtarget < 1) || (err<0.5));	
			} else { //not found in treemap 
				
	//STEP 2. look for the greatest possible E24 value to subtract, 
				d2=Rtarget; 
				muld2=1;
				while (d2>=10){
					d2=d2/10;
					muld2=muld2*10;;
				}
				d2 = truncateDecimal(d2,1); //#.#
				if (isInE24(d2, E24)){
					values2 =addVal(values2,d2*muld2); 
				} else {
	// subtract highest E24 value below Rtarget	
					d=0;
					if ((d2==1.0)&& (muld2>1)) {
						values2 = addVal(values2, 9.1*muld2/10); 
					} else {
						for (int i=0;i<E24.length;i++) {
							if ((E24[i]< d2)){
								d = (E24[i]);
							}
						}
						if (d!=0) values2 = addVal(values2, d*muld2); 
					}
				}
			} 
				Rtarget = target - sum(values2);
				err = (Math.abs(target-sum(values2)) / target)*100;
				ready = ((Rtarget < 1) || (err<0.5));	
		} while (!ready);
				
		 
		/* =========================THIRD APPROACH============================= 
			Uses also a TreeMap.
			The do..while loop consists of:
			1. The very first step is looking for an existing E24 value, 
				 as is done also in the two other approaches.
			2. Next, first the treemap is inspected. 
			3. If that fails, then the biggest possible E24 value is subtracted. 
			======================================================================
		*/
	  
	  ready=false; 
		Rtarget =target;
		do {
//----0. create new patterns
			d3=Rtarget; //d2 and d3 for patterns #.# and ##.#
			muld3=1;
			while (d3>=100){
				d3=d3/10;
				muld3=muld3*10;
			}
			d3 = truncateDecimal(d3,1); //##.# or #.#
			if (d3 >=10) {
				d2 = d3/10;
				muld2=muld3*10;
			}	else{
				d2=d3;
				muld2=muld3;
			}
			d2 = truncateDecimal(d2,1); //#.#
			String s="";
			
	//STEP 1. look for an existing E24 value
			if (isInE24(d2,E24)) {  
				values3 = addVal(values3,(d2*muld2));
	//STEP 2. look in the TreeMap with pattern #.# and ##.# 
			} else if (tm.get(new Double(d3))!=null){
					s = tm.get(new Double(d3));
					//parse s
					double db1 = muld3*(new Double(s.substring(0, s.indexOf('+'))).doubleValue());
					double db2 = muld3*(new Double(s.substring(s.indexOf('+')+1)).doubleValue());
					values3 = addVal(values3, db1);
					values3 = addVal(values3, db2);
			} else if (tm.get(new Double(d2))!=null){
					s = tm.get(new Double(d2));
					//parse s
					double db1 = muld2*(new Double(s.substring(0, s.indexOf('+'))).doubleValue());
					double db2 = muld2*(new Double(s.substring(s.indexOf('+')+1)).doubleValue());
					values3 = addVal(values3, db1);
					values3 = addVal(values3, db2);
			}else {
	//STEP 3. finally subtract the greatest possible E24 value and loop until ready	
			double db=0;
			if ((d2==1.0)&& (muld2>1)) {
				values3 = addVal(values3, 9.1*muld2/10);
			} else {
				for (int i=0;i<E24.length;i++) {
					if ((E24[i]< d2)){
						db = (E24[i]);
					}
				}
				if (db!=0) values3 = addVal(values3, db*muld2);
			}
		}
			
			Rtarget = target - sum(values3);
			ready = Rtarget <1;
		} while (!ready);
		
		//creating popupmsg  round(values1[i],0)
		popupmsg = popupmsg.concat("\nWITH SERIES RESISTORS:\n   ");
		
		values1 = sortHiLo(values1); 
		values2 = sortHiLo(values2);
		values3 = sortHiLo(values3);
	
		if (values1.length >0) {
			popupmsg=popupmsg.concat("\n   "+df1.format(sum(values1))+" = ");	
			for (int i=0;i<values1.length;i++) {
				popupmsg=popupmsg.concat(formatR(values1[i]));
				if (i < values1.length-1) popupmsg=popupmsg.concat(" + "); 
			}
			err = (Math.abs(target-sum(values1)) / target)*100;
			popupmsg=popupmsg.concat("\n      Error is "+df2.format(err)+"%");
		} else { //matrix result
			popupmsg=popupmsg.concat(popupmsg2);
		}
		
		popupmsg=popupmsg.concat("\n   "+df1.format(sum(values2))+" = ");	
		for (int i=0;i<values2.length;i++) {
			popupmsg=popupmsg.concat(formatR(values2[i]));
			if (i < values2.length-1) popupmsg=popupmsg.concat(" + "); 
		}
		err = (Math.abs(target-sum(values2)) / target)*100;
		popupmsg=popupmsg.concat("\n      Error is "+df2.format(err)+"%");
		
		popupmsg=popupmsg.concat("\n   "+df1.format(sum(values3))+" = ");	
		for (int i=0;i<values3.length;i++) {
			popupmsg=popupmsg.concat(formatR(values3[i]));
			if (i < values3.length-1) popupmsg=popupmsg.concat(" + "); 
		}
		err = (Math.abs(target-sum(values3)) / target)*100;
		popupmsg=popupmsg.concat("\n      Error is "+df2.format(err)+"%");
		
			
	}// endof calculateSer
	
	
	public parRFrame() { 
		setTitle("PARALLEL AND SERIES RESISTORS CALCULATOR");
    setSize(700, 500);
    setFont(new Font("Verdana",Font.PLAIN,12));
    addWindowListener(new WindowAdapter()
       {  public void windowClosing(WindowEvent e)
          {  System.exit(0);
          }
       } );

			log = new JLabel();
			log.setFont(new Font("Verdana",Font.PLAIN,10));
			log.setText("Big complex values may take some time");

      targetInput = new JTextField(20);
      targetInput.setFont(new Font("Verdana",Font.PLAIN,11));
      targetInput.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
             try {
            		double d = new Double(targetInput.getText()).doubleValue();
            		calculate(d);
            	}	catch(Exception ee) {  
            		JOptionPane.showMessageDialog(panel,ee,"Error", JOptionPane.ERROR_MESSAGE);
            	}
            }
        }
   		});
   		addWindowListener( new WindowAdapter() {
    		public void windowOpened( WindowEvent e ){
        targetInput.requestFocus();
   	 		}
			}); 

      calcButton = new JButton("Calc");
      calcButton.addActionListener(new ActionListener()
         {  public void actionPerformed(ActionEvent event)
            { try {
            		double d = new Double(targetInput.getText()).doubleValue();
            		//targetInput.setText("");
            		calculate(d);
            	}	catch(Exception e) {  
            		JOptionPane.showMessageDialog(panel,e,"Error", JOptionPane.ERROR_MESSAGE);
            	}
           }
         });

		javax.swing.border.Border empty = javax.swing.BorderFactory.createEmptyBorder(10,10,10,10);

		try {
			editorPane = new JEditorPane();
		} catch (Exception e) {
			//System.out.println(e);
			JOptionPane.showMessageDialog(panel,e,"Error", JOptionPane.ERROR_MESSAGE);
		}
		editorPane.setBorder(empty);
		editorPane.setEditable(false);
		editorPane.setContentType("text/html");
  	Container contentPane = getContentPane();
  	contentPane.add(new JScrollPane(editorPane), "Center");
		editorPane.setText("<style>p {font-family:Verdana;font-size:11px;}li, ul {font-family:Verdana;font-size:10px}</style><p style=''>If a resistor value, (a [big] detailed value or one outside the E24 range) is needed, a solution is to place resistors in parallel or in series.<br>This app examines both solutions.</p><p><b>Parallel resistors</b>:<br>All possible combinations of two and three resistors are inspected. The very best solution is presented. If unsuccessful, the app lowers the target value in one Ohm steps, down to -5%. If relevant, an extra series resistor is advised.</p><p><b>Series resistors</b>:<br> Three different algorithms are executed. It depends on the target value whether the outcomes differ or not. All calculated arrays are presented.</p><p>	It is up to the end user to select his/her most appropriate outcome. In the rare case of little success, it may help to start with a somewhat (acceptable) higher target value.</p><p>	Only digits are accepted as input (e.g. 2k7 will be rejected)</p><p>&copy; 2021 JanHerman Verpoorten<br>Version 2.0<br>This app is Freeware.");
      
  	panel = new JPanel();
  	//panel.setBorder(new EmptyBorder(7,7,7,7));
  	panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(7,7,10,7);
    
    //log = new JTextArea(1, 15);//showing progress
    //log.setBackground(Color.lightGray);
    c.weightx = 2.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		panel.add(log, c);
    
    JLabel label = new JLabel("Target value:");
    c.weightx = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		panel.add(label,c);
		
    c.weightx = 2.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = 0;
		panel.add(targetInput,c);
				
    c.weightx = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 3;
		c.gridy = 0;
		c.anchor = GridBagConstraints.PAGE_END; //bottom of space
		panel.add(calcButton,c);

		contentPane.add(panel, "South");

  }
	private JLabel log; 
  private JTextField targetInput;
  private JButton calcButton;
  private JEditorPane editorPane;

}
