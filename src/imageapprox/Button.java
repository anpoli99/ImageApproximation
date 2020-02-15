package imageapprox;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.border.LineBorder;

public class Button {
	JLabel label = new JLabel();
	int x1, x2, y1, y2;
	public Button(String text, int X1, int Y1, int X2, int Y2) {
		label.setText(text);
		label.setFont(new Font("Verdana",1,25));
		x1 = X1; y1 = Y1; x2 = X2; y2 = Y2;
		label.setBounds(x1, y1 - y2, x2, y2);
		label.setBorder(new LineBorder(Color.BLACK));
	}
	public Button(String text) {this(text,500,500,600,600);}
	public Button(int x1, int y1, int x2, int y2) {this("", x1, y1, x2, y2);}
	
	
	public JLabel getLabel() {return label;}
	public boolean inBounds(int x, int y) {
		return x > x1 && x < x1 + x2 && y > y1 && y < y1 + y2;
	}
	
	
}
