/**
 * 	Calculates the best target value (user input), using one, or two/three parallel E24 resistors,
 *	possibly with an extra series resistor. 
 *
 * Version 1.0, 06-05-2021
 * (c) 2021 JanHerman Verpoorten
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;
import java.text.DecimalFormat;
import java.math.RoundingMode;

public class parR {
  public static void main(String[] args)
   {  JFrame frame = new parRFrame();
      frame.show();
   }
}
	

class parRFrame extends JFrame { 
	double[] E24 = {1.0,1.1,1.2,1.3,1.5,1.6,1.8,2.0,2.2,2.4,2.7,3.0,3.3,3.6,3.9,4.3,4.7,5.1,5.6,6.2,6.8,7.5,8.2,9.1};
	int[] mult = {1,10,100,1000,10000,100000,1000000,10000000};
	JPanel panel;
	
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
			case 0: return s.concat(" Ohm");
			case 1: return sub.concat(" Ohm");
			case 2: return sub.concat("0 Ohm");
			case 3: return s.substring(0,s.indexOf('.')).concat("K").concat(s.substring(s.indexOf('.')+1,3));
			case 4: return sub.concat("K");
			case 5: return sub.concat("0K");
			case 6: return s.substring(0,s.indexOf('.')).concat("M").concat(s.substring(s.indexOf('.')+1,3));
			case 7: return sub.concat("M");
			default:
			return Double.toString(arg);
		}
	}
		
	public double[] calcSeriesR(double arg, double result) {
		double[] series={0,0};
		double dif=arg-result; 
		int m=0;
		while (dif>10){
			dif= dif/10;
			m +=1;
			}
		for (int i=0;i<E24.length;i++) {
			if (E24[i]<=dif) {
				series[0]=E24[i]*mult[m];
				//look for next value in E24 range
				if (i<E24.length-1) {
					series[1]=E24[i+1]*mult[m];
				}else if (m<mult.length-1){
					series[1]=E24[0]*mult[m+1];
				}else series[1]=Double.NaN;
			}else
			break;
		}
		return series;
	}	
	
	public void popup(String msg, double target) {
		targetInput.setText("");
		//log.setText("Done");
		
		javax.swing.border.Border empty = javax.swing.BorderFactory.createEmptyBorder(10,10,10,10);
		String ms = "The best result for target "+target+" Ohm:\n";
		ms = ms.concat(msg);
		JTextArea jta = new JTextArea(ms, 15,40);
		jta.setLineWrap(true);
		jta.setWrapStyleWord(true);
		jta.setFont(new Font("Verdana",Font.PLAIN,12));
		jta.setBorder(empty);

		JScrollPane scrollPane = new JScrollPane(jta);

		JOptionPane.showMessageDialog(panel, scrollPane,"CALCULATION",JOptionPane.PLAIN_MESSAGE);
	}
	
	
	public void calculate(Double target) {
		double Rtarget=0, Rres=0, Ra=0, Rb=0, Rc=0, R1=0, R2=0, R3=0, diff=0;
		double err, d;
		double delta=999999999.99;//start val;
		String popupmsg;
		
		
		if (target <= 0) {
			JOptionPane.showMessageDialog(panel, "Negative values are invalid","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (target > mult[mult.length-1]) {
			JOptionPane.showMessageDialog(panel, "Target value "+target+" is too big","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		Rtarget=target;
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);
		
	//int t=0;
	do{
		//t+=1;
		//log.setText(t+". Trying "+Rtarget+" ...");
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
				popupmsg=("\nNo parallel resistors needed: "+formatR(Rtarget)+" is a E24 value; error is "+df.format(err)+"%\n\n" );
				if (err !=0) {
					double[] sr=calcSeriesR(target, Rtarget);
					popupmsg = popupmsg.concat("For the very best result, add a series resistor:\n\n");
					popupmsg=popupmsg.concat(formatR(sr[0])+" Ohm, ");
					err = (Math.abs(target-(Rtarget+sr[0])) / target)*100;
					popupmsg=popupmsg.concat("creating a final error value "+df.format(err)+"%,\n" );
					if (sr[1] !=Double.NaN) {
						popupmsg=popupmsg.concat("Or\n"+ formatR(sr[1])+" Ohm, ");
						err = (Math.abs(target-(Rtarget+sr[1])) / target)*100;
						popupmsg=popupmsg.concat("creating a final error value "+df.format(err)+"%" );
					}
				}
				popup(popupmsg,target);
				return;
			}
		}
		//first try with two resistors
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
							popupmsg=("\nRa="+formatR(R1) +"\nRb="+formatR(R2)+"\nResult is  "+res+" ; error is "+df.format(err)+"%\n\n");
							if (err !=0) {
								double[] sr=calcSeriesR(target, Rtarget);
								popupmsg = popupmsg.concat("For the very best result, add a series resistor:\n\n");
								popupmsg=popupmsg.concat(formatR(sr[0])+" Ohm, ");
								err = (Math.abs(target-(Rtarget+sr[0])) / target)*100;
								popupmsg=popupmsg.concat("creating a final error value "+df.format(err)+"%\n" );
								if (sr[1] !=Double.NaN) {
									popupmsg=popupmsg.concat("Or\n"+ formatR(sr[1])+" Ohm, ");
									err = (Math.abs(target-(Rtarget+sr[1])) / target)*100;
									popupmsg=popupmsg.concat("creating a final error value "+df.format(err)+"%" );
								}
							}
							popup(popupmsg,target);
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
									popupmsg=("\nRa="+formatR(R1) +"\nRb="+formatR(R2)+"\nRc="+formatR(R3)+"\nResult is  "+res+" ; error is "+df.format(err)+"%\n\n");
									if (err !=0) {
										double[] sr=calcSeriesR(target, Rtarget);
										popupmsg = popupmsg.concat("For the very best result, add a series resistor:\n\n");
										popupmsg=popupmsg.concat(formatR(sr[0])+", ");
										err = (Math.abs(target-(Rtarget+sr[0])) / target)*100;
										popupmsg=popupmsg.concat("creating final error value "+df.format(err)+"%\n" );
										if (sr[1] !=Double.NaN) {
											popupmsg=popupmsg.concat("Or\n"+ formatR(sr[1])+", ");
											err = (Math.abs(target-(Rtarget+sr[1])) / target)*100;
											popupmsg=popupmsg.concat("creating final error value "+df.format(err)+"%" );
										}
									}
									popup(popupmsg,target);
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
		
		if (Rc==0)
			popupmsg=("\nBest found two values are "+formatR(Ra)+" and "+formatR(Rb)+"\n\n");
			else 
			popupmsg="\nBest found three values are "+formatR(Ra)+", "+formatR(Rb)+" and "+formatR(Rc)+"\n\n";

			popupmsg=popupmsg.concat("Last attempted target value is "+Rtarget+", achieved is "+Rres+".\nDifference: "+Math.abs(Rtarget-Rres)+" Ohm\n");
			err = (Math.abs(Rtarget-Rres) / Rtarget)*100;
			popupmsg=popupmsg.concat("Error is "+ df.format(err) + "%\n");
			if ((err !=0) && (err <=5.0)) {
				double[] sr=calcSeriesR(target, Rres);
				popupmsg=popupmsg.concat("Error is <= 5%\n");
				popupmsg = popupmsg.concat("For the very best result, add a series resistor:\n\n");
 				popupmsg = popupmsg.concat(formatR(sr[0])+" ,\n");
				err = (Math.abs(target-(Rtarget+sr[0])) / target)*100;
				popupmsg=popupmsg.concat("creating a final error value "+df.format(err)+"%\n" );
				if (sr[1] !=Double.NaN) {
					popupmsg=popupmsg.concat("Or\n"+ formatR(sr[1])+", ");
					err = (Math.abs(target-(Rtarget+sr[1])) / target)*100;
					popupmsg=popupmsg.concat("creating a final error value "+df.format(err)+"%\n\n" );
				}
				popupmsg=popupmsg.concat("Or run this app with a somewhat higher target value." );
				popup(popupmsg,target);
			}else {
				popup("Try this app with a somewhat higher (acceptable) target value,\n or find another solution.",target );
			}
	
	}//endof calculate
	
	
	public parRFrame() { 
		setTitle("PARALLEL RESISTORS CALCULATOR");
    setSize(700, 500);
    setFont(new Font("Verdana",Font.PLAIN,12));
    addWindowListener(new WindowAdapter()
       {  public void windowClosing(WindowEvent e)
          {  System.exit(0);
          }
       } );

			log = new JLabel();
			log.setFont(new Font("Verdana",Font.PLAIN,10));
			log.setText("Big values may take some time");

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
			System.out.println(e);
		}
		editorPane.setBorder(empty);
		editorPane.setEditable(false);
		editorPane.setContentType("text/html");
  	Container contentPane = getContentPane();
  	contentPane.add(new JScrollPane(editorPane), "Center");
		editorPane.setText("<style>p {font-family:Verdana;font-size:11px;}li, ul {font-family:Verdana;font-size:10px}</style><p style=''>If a resistor value outside the E24 range is needed, a solution is to place two or more resistors in parallel or series.<br>This app calculates the values of one, or two resp. three parallel resistors, to establish a desired target value. If relevant, an extra series resistor is advised.</p><p>All combinations of existing E24 values are looped through. </p><p>Not every target value is immediately successful with two or three resistors:<ul><li>If unsuccessful, the app lowers the target value in one Ohm steps, down to -5%.<br>If this works, the advised extra series resistor is relevant.</li><li>No success? Start with a somewhat higher target (or use more parallel resistors)</li></ul></p><p>Only digits are accepted as input (e.g. 2k7 will be rejected)</p><p>&copy; 2021 J.H. Verpoorten<br>This app is Freeware.");
      
 /* 	// put all components in a panel
		log = new JTextArea(1, 15);//showing progress
  	panel = new JPanel();
 	 	panel.add(new JLabel("Target value: "));
 	 	panel.add(targetInput);
 	 	panel.add(calcButton);
  	panel.add(log, BorderLayout.LINE_END);

		contentPane.add(panel, "South");
*/
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
