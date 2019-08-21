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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
*/
public class GRDM_U2 implements PlugIn {

    ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;
	
	
    public static void main(String args[]) {
		//new ImageJ();
    	IJ.open("/users/mcflu/IdeaProjects/GLDM_UE1/src/orchid.jpg");
    	//IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");
		GRDM_U2 pw = new GRDM_U2();
		pw.imp = IJ.getImage();
		pw.run("");
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
    
    
    class CustomWindow extends ImageWindow implements ChangeListener {
         
        private JSlider jSliderBrightness;
		private JSlider jSliderContrast;
		private JSlider jSliderHue;
		private JSlider jSliderSaturation;

		//Standard Werte für die Klassenattribute
		private double brightness = 1;
		private double contrast = 1 ;
		private double hue = 0;
		private double saturation = 1;

		CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }
    
        void addPanel() {
        	//JPanel panel = new JPanel();
        	Panel panel = new Panel();

            panel.setLayout(new GridLayout(4, 1));
            jSliderBrightness = makeTitledSilder("Helligkeit", 0, 256, 128);
            jSliderContrast = makeTitledSilder("Kontrast", 0, 100, 50);
			jSliderSaturation = makeTitledSilder("Sättigung", 0,  100, 50);
			jSliderHue =makeTitledSilder("Hue", 0, 360, 0);

			panel.add(jSliderBrightness);
            panel.add(jSliderContrast);
			panel.add(jSliderSaturation);
			panel.add(jSliderHue);

			add(panel);
            
            pack();
         }
      
        private JSlider makeTitledSilder(String string, int minVal, int maxVal, int val) {
		
        	JSlider slider = new JSlider(JSlider.HORIZONTAL, minVal, maxVal, val );
        	Dimension preferredSize = new Dimension(width, 50);
        	slider.setPreferredSize(preferredSize);
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(), 
					string, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
			slider.setMajorTickSpacing((maxVal - minVal)/10 );
			slider.setPaintTicks(true);
			slider.addChangeListener(this);
			
			return slider;
		}
        
        private void setSliderTitle(JSlider slider, String str) {
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(),
				str, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
		}

		public void stateChanged( ChangeEvent e ){
			JSlider slider = (JSlider)e.getSource();

			if (slider == jSliderBrightness) {
				brightness = slider.getValue()-128; //Damit der Slider von .128 bis 128 verstellbar ist
				String str = "Helligkeit " + brightness; 
				setSliderTitle(jSliderBrightness, str); 
			}
			
			if (slider == jSliderContrast) {
				contrast = slider.getValue();
				//Quantisiertung
				//1.Bereich Intervall [0, 0.2, 0.4, 0.6, 0.8, 1]
				if ( contrast <= 60) {
					contrast = contrast / 50;
					// 2. Bereich:  [2, 4, 6, 8, 10]
				}else{
					contrast = (contrast - (100.0 - contrast))/10.0;
				}
				String str = "Kontrast " + contrast;
				setSliderTitle(jSliderContrast, str);
			}

			if (slider == jSliderSaturation) {
				saturation = slider.getValue();
				//1.Bereich Intervall  [0, 0.25, 0.5, 0.75, 1]
				if (saturation <= 60){
					saturation/= 50.0;
					//2.Bereich Intervall [2, 3, 4, 5]
				}else{
					saturation = (saturation- 50.0)/10.0;
				}
				String str = "Sättigung " + saturation;
				setSliderTitle(jSliderSaturation, str);
			}

			if (slider == jSliderHue) {
				hue = slider.getValue();
				String str = "Hue " + hue;
				setSliderTitle(jSliderHue, str);
			}
			
			changePixelValues(imp.getProcessor());
			
			imp.updateAndDraw();
		}

		
		private void changePixelValues(ImageProcessor ip) {
			
			// Array fuer den Zugriff auf die Pixelwerte
			int[] pixels = (int[])ip.getPixels();
			
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 
					
					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;
					
					
					// anstelle dieser drei Zeilen später hier die Farbtransformation durchführen,
					// die Y Cb Cr -Werte verändern und dann wieder zurücktransformieren

					//umrechnend der RGB Werte in YUV
					double Y = 0.299 * r + 0.587 * g + 0.114 * b; //Luminanz
					double U = (b - Y) * 0.493; //Chrominanz U
					double V = (r - Y) * 0.877; //Chrominanz V

					//Y = Y + brightness; //habe ich benutzt als man nur die brightness brauchte jetzt ist brightness ja in der Formel mit contrast drin

					//Formel für Helligkeit und Kontraast
					//127.5 kommt aus der VL
					Y = ((Y - 127.5) * contrast) + brightness + 127.5; //Kontrast Formel aus der Vorlesung

					//Formel für Saturation
					U = U * saturation;
					V = V * saturation;

					//Formel für Hue (Winkel müssen in Bogenaß umgerechnet werden)
					U = (Math.cos(Math.toRadians(hue))*U) - (Math.sin(Math.toRadians(hue))* V);
					V =  (Math.sin(Math.toRadians(hue))*U) + (Math.cos(Math.toRadians(hue))*V);

					//umrechnen von YUV in RGB
					int rn = (int) (Y + V/0.877);
					int bn = (int) (Y + U/0.493);
					int gn = (int)((1/0.587 * Y)- (0.299/0.587 * rn )-( 0.114/0.587 * bn));

					//umrechnen von YUV in RGB
					/*int rn = (int) (Y + 1.13983 * V);
					int gn = (int) (Y - 0.39466 * U - 0.5806 * V);
					int bn = (int) (Y + 2.03211 * U);*/

					// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
					//Formel aus der VL
					rn = Math.min(Math.max( rn,0),255);
					bn = Math.min(Math.max( bn,0),255);
					gn = Math.min(Math.max( gn,0),255);

					//alte Version
					/*if( rn > 255){
						rn = 255;
					}else if(rn < 0){
						rn = 0;
					}
					if( gn > 255){
						gn = 255;
					}else if(gn < 0){
						gn = 0;
					}
					if( bn > 255){
						bn = 255;
					}else if(bn < 0){
						bn = 0;
					}*/
					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}
		}
		
    } // CustomWindow inner class
} 
