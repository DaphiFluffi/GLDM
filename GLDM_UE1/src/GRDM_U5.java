import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
 */
public class GRDM_U5 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = {"Original", "Tiefpass", "Hochpass", "Kantenschaerfung"};


	public static void main(String args[]) {

		IJ.open("/users/mcflu/IdeaProjects/GLDM_UE1/src/sail.jpg");
		//IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

		GRDM_U5 pw = new GRDM_U5();
		pw.imp = IJ.getImage();
		pw.run("");
	}

	//Funktion um eine 3x3 Matrix zu erstellen
	public double[][] createKernel (double a, double b, double c, double d, double e, double f, double g, double h, double i){
		double[][] kernel = {{a, b , c}, {d, e, f}, {g, h, i}};
		return kernel;
	}

	public void randbehandlung(int[] pixels) {

		int[]pixelsMehr = new int[pixels.length + (width+2)*2 + (height*2)];
		// oberer und unterer Rand mit Ecken
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				// y= y+(height-1) spting immer an die selbe Position in der untersten Reihe
				int pos = y * width + x;
				int posMehr = (y+1) * width + (x+1);
				pixelsMehr[posMehr] = pixels[pos];// wenn wir in der unteren Reihe sind,

				if (y == 0) {
					//nimm immer die pixel unter dir
					posMehr = (y-1) * width + (x);
					pixelsMehr[posMehr] = pixels[pos]; // Wenn wir in der Oberen Zeile sind,

				} else if ( x == 0){
					//nimm die Pixel aus der Reihe über dir
					posMehr = (y) * width + (x-1);

				}

			}
		}

		// linker und rechter Rand ohne die Eckpixel, weil schon bedacht in oben unten
		/*for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				//x = x+ (width-1) springt immer auf den linken Rand an derselben Stelle, wo man gerade ist
				int pos = y * width + x;

				if (x == 0) { //Wenn du in der rechten Spalte bist,
					//nimm den Pixelrechts von dir
					pixels[pos] = pixels[pos + 1];
				} else {//wenn du in der linken Spalte bist,
					//nimm den Pixel links von dir
					pixels[pos] = pixels[pos - 1];

				}

			}
		}*/

	}

	public void run(String arg) {
		if (imp==null) 
			imp = WindowManager.getCurrentImage();
		if (imp==null) {
			return;
		}
		CustomCanvas cc = new CustomCanvas(imp);

		storePixelValues(imp.getProcessor());

		new CustomWindow(imp, cc);
	}


	private void storePixelValues(ImageProcessor ip) {
		width = ip.getWidth();
		height = ip.getHeight();

		origPixels = ((int []) ip.getPixels()).clone();
	}


	class CustomCanvas extends ImageCanvas {

		CustomCanvas(ImagePlus imp) {
			super(imp);
		}

	} // CustomCanvas inner class


	class CustomWindow extends ImageWindow implements ItemListener {

		private String method;
		
		CustomWindow(ImagePlus imp, ImageCanvas ic) {
			super(imp, ic);
			addPanel();
		}

		void addPanel() {
			//JPanel panel = new JPanel();
			Panel panel = new Panel();

			JComboBox cb = new JComboBox(items);
			panel.add(cb);
			cb.addItemListener(this);

			add(panel);
			pack();
		}

		public void itemStateChanged(ItemEvent evt) {

			// Get the affected item
			Object item = evt.getItem();

			if (evt.getStateChange() == ItemEvent.SELECTED) {
				System.out.println("Selected: " + item.toString());
				method = item.toString();
				changePixelValues(imp.getProcessor());
				imp.updateAndDraw();
			} 

		}

		private void changePixelValues(ImageProcessor ip) {

			// Array zum Zurückschreiben der Pixelwerte
			int[] pixels = (int[])ip.getPixels();

			if (method.equals("Original")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						
						pixels[pos] = origPixels[pos];

					}
				}
			}

			if (method.equals("Tiefpass")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {

						int argb = 0;
						double  r = 0 ;
						double g = 0;
						double b = 0;

						//TP: Offset = 0
						//??funktioniert an sich richtig aber es verschiebt sich um 2 pixel diagonal??
						double [][] meinKernel = createKernel((1./9.),(1./9.),(1./9.),(1./9.),(1./9.),(1./9.),(1./9.),(1./9.),(1./9.));

						//irgendein Hochpassfilter, offset 128
						//double [][] meinKernel = createKernel(-1,-2,-1,-2,12,-2,-1,-2,-1);
						//double [][] meinKernel = createKernel(0,1,0,0,0,0,0,0,0);

						int pos = 0;
						int rn = 0;
						int gn = 0;
						int bn = 0;

						double rd = 0 ;
						double gd = 0;
						double bd = 0;
						//int offset = 128;
						int offset = 0;

						for(int k = -1; k < 2; k++){
							for(int l = -1; l < 2; l++){

								pos = (y+k)*width + (x+l);
								if(pos >= origPixels.length){ //kein IndexOutOfBounds mehr
									pos = origPixels.length -1;
								}else if(pos< 0){
									pos = 0;
								}
								argb = origPixels[pos]; //PIxelwert an der Position auslesen
								r = (argb >> 16) & 0xff; // aus dem Pixelwert die rgb Farben extrahieren
								g = (argb >>  8) & 0xff;
								b =  argb        & 0xff;

								/*rn += (int)(meinKernel[k+1][l+1] * (r + offset));
								gn += (int) (meinKernel[k+1][l+1] * (g + offset));
								bn += (int) (meinKernel[k+1][l+1] * (b + offset));*/

								//erst ein Mal double wert berechnen
								rd += (meinKernel[k+1][l+1] * (r + offset)); // den Kernel mit der Farbe multiplizieren + offset
								gd += (meinKernel[k+1][l+1] * (g + offset));
								bd += (meinKernel[k+1][l+1] * (b + offset));

							  /*  rd +=  meinKernel[l+1][k+1]* r + offset;
								gd += meinKernel[l+1][k+1]* g + offset ;
								bd += meinKernel[l+1][k+1]* b  + offset;*/

							}
						}

						//double wert nach int casten
						rn = (int) rd;
						bn = (int) bd;
						gn = (int) gd;

						//Über und Unterlauf vermeiden
						rn = Math.min(Math.max(rn, 0), 255);
						bn = Math.min(Math.max(bn, 0), 255);
						gn = Math.min(Math.max(gn, 0), 255);
						//Pixelwerte ins Array zuruckschreiben
						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;
					}
				}
				//Pixel Array in die Randbehandlungsfunktion einsetzen
				randbehandlung(pixels);

			}

			if(method.equals ("Hochpass")){

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {

						int argb = 0;
						double r = 0 ;
						double g = 0;
						double b = 0;

						double o = ((1./9.)* (-1)); //sicher gehen
						double [][] meinKernel = createKernel(o,o,o,o,(8./9.),o,o,o,o);

						int pos = 0;
						int rn = 0;
						int gn = 0;
						int bn = 0;
						double rd = 0 ;
						double gd = 0;
						double bd = 0;
						int offset = 128;
						for(int k = -1; k < 2; k++){
							for(int l = -1; l < 2; l++){

								pos = (y+k)*width + (x+l);
								if(pos >= origPixels.length){ //kein IndexOutOfBounds mehr
									pos = origPixels.length -1;
								}else if(pos< 0){
									pos = 0;
								}
								argb = origPixels[pos];
								r = (argb >> 16) & 0xff;
								g = (argb >>  8) & 0xff;
								b =  argb        & 0xff;
								rd += (meinKernel[k+1][l+1] * (r + offset));
								gd += (meinKernel[k+1][l+1] * (g + offset));
								bd += (meinKernel[k+1][l+1] * (b + offset));

							  /*  rd +=  meinKernel[l+1][k+1]* r + offset;
								gd += meinKernel[l+1][k+1]* g + offset ;
								bd += meinKernel[l+1][k+1]* b  + offset;*/

								rn = (int) rd;
								bn = (int) bd;
								gn = (int) gd;

								rn = Math.min(Math.max(rn, 0), 255);
								bn = Math.min(Math.max(bn, 0), 255);
								gn = Math.min(Math.max(gn, 0), 255);
							}
						}

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;
					}
				}
				randbehandlung(pixels);

			}

			if(method.equals ("Kantenschaerfung")){
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {

						int argb = 0;
						double r = 0 ;
						double  g = 0;
						double  b = 0;

						//Kantenschärfung Offset = 0
						double o = ((1./9.)* (-1));
						double [][] meinKernel = createKernel(o,o,o,o,(17./9.),o,o,o,o);

						double rd = 0 ;
						double gd = 0;
						double bd = 0;

						int pos = 0;

						int rn = 0;
						int gn = 0;
						int bn = 0;

						int offset = 0;
						for(int k = -1; k < 2; k++){
							for(int l = -1; l < 2; l++){

								pos = (y+k)*width + (x+l);
								if(pos >= origPixels.length){ //kein IndexOutOfBounds mehr
									pos = origPixels.length -1;
								}else if(pos< 0){
									pos = 0;
								}
								argb = origPixels[pos];
								r = (argb >> 16) & 0xff;
								g = (argb >>  8) & 0xff;
								b =  argb        & 0xff;

								rd += (meinKernel[k+1][l+1] * (r + offset));
								gd += (meinKernel[k+1][l+1] * (g + offset));
								bd += (meinKernel[k+1][l+1] * (b + offset));

								rn = (int) rd;
								bn = (int) bd;
								gn = (int) gd;

								rn = Math.min(Math.max(rn, 0), 255);
								bn = Math.min(Math.max(bn, 0), 255);
								gn = Math.min(Math.max(gn, 0), 255);
							}
						}

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;
					}
				}
				randbehandlung(pixels);

			}

		}

	} // CustomWindow inner class
} 
