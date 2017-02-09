package com.xulusoft.FaceFinder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import javax.imageio.ImageIO;



public class Detector {
	public String searchFile=""; 
	public int 	maxStage=0, judgeCount=1;
	
	public Detector() {
		
	}
	public Vector<HaarFace> Faces = new Vector<HaarFace>();
	
	public Vector<HaarRect> faces=new Vector<HaarRect>();
	
	public Detector(String... paths) {
		if (paths.length>0) {
			hc = new HaarCascade(paths[0]);
			faces =  new Vector<HaarRect>();
		}
		if (paths.length>1) {
			e_hc = new HaarCascade(paths[1]);
		}
		if (paths.length>2) {
			m_hc = new HaarCascade(paths[2]);
		}
		if (paths.length>3) {
			n_hc = new HaarCascade(paths[3]);
		}
	}
	public Detector(InputStream... iss) {
		if (iss.length>0) {
			hc = new HaarCascade(iss[0]);
			faces =  new Vector<HaarRect>();
		}
		if (iss.length>1) {
			e_hc = new HaarCascade(iss[1]);
		}
		if (iss.length>2) {
			m_hc = new HaarCascade(iss[2]);
		}
		if (iss.length>3) {
			n_hc = new HaarCascade(iss[3]);
		}
	}
	
	public HaarCascade hc=null;
	public HaarCascade e_hc=null;
	public HaarCascade m_hc=null;
	public HaarCascade n_hc=null;
	
	public double StepScale = 1.2;
	
	public ImgData idata=null;
	
	public Vector<HaarFace> Find(String path) throws IOException{
		searchFile = path;
		return Find(ReadImg(path));
	}

	public Vector<HaarFace> Find(int[][] img){
		if (Faces != null) Faces.clear();
		if (faces != null) faces.clear();
		idata =null;
		if (hc==null) return null;
		try {
			idata= new ImgData(img);
		}
		catch(Exception ee) {
			idata=null;
		}
		if (idata==null) return null;
		Vector<HaarRect> result = new Vector<HaarRect>();
		SearchHaarCascade(result);
		SortHaarFaces(result);
		faces = Consolidation(result);
		System.out.println("ff found faces: "+faces.size());
		Vector<HaarRect> haarE=null, haarM=null,haarN=null;
		for (int i=0; i<faces.size();i++)
		{
			int cf = 0;
			if (e_hc != null)	{
				haarE=SearchAreaHaarCascade(faces.get(i),e_hc,1.2);
				if (haarE.size()>0) {
					cf += haarE.size();
					if (haarE.size()>2) {
						//printRect(haarE);
						SortHaarRects(haarE);
						//System.out.println("after sort");
						//printRect(haarE);
					}
				}
			}
			if (m_hc != null) {
				haarM=SearchAreaHaarCascade(faces.get(i),m_hc,1.2);
				if (haarM.size()>0) {
					cf += haarM.size();
					if (haarM.size()>1) SortHaarRects(haarM);
				}
			}
			if (n_hc != null)	{
				haarN=SearchAreaHaarCascade(faces.get(i),n_hc,1.2);
				if (haarN.size()>0) {
					cf += haarN.size();
					if (haarN.size()>1) SortHaarRects(haarN);
				}
			}
			if (cf>=judgeCount) {
				HaarFace ff =new HaarFace(faces.get(i));
				ff.SetEyes(haarE);
				ff.SetMouth(haarM);
				ff.SetNose(haarN);
				if (ff.IsFace()) 
					Faces.add(ff);
				haarE.clear();haarE=null;
				haarM.clear();haarM=null;
				haarN.clear();haarN=null;
			}
		}
		faces.clear();
		faces = null;
		System.out.println( Faces.get(0).FaceRect.x+","+Faces.get(0).FaceRect.y+","+Faces.get(0).FaceRect.width+","+Faces.get(0).FaceRect.height);

		return Faces;
	}
	
	public void 			MergeHaarRect(Vector<HaarRect> haar1, Vector<HaarRect> haar2) {
		for (int i=0; i< haar2.size();i++) {
			haar1.add(haar2.get(i));
		}
		haar2.clear();
		haar2=null;
	}
	
	public static void 		checkMemory() {
        
        int mb = 1024*1024;
         
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
         
        System.out.println("##### Heap utilization statistics [MB] #####");
         
        //Print used memory
        System.out.println("Used Memory:"
            + (runtime.totalMemory() - runtime.freeMemory()) / mb);
 
        //Print free memory
        System.out.println("Free Memory:"
            + runtime.freeMemory() / mb);
         
        //Print total available memory
        System.out.println("Total Memory:" + runtime.totalMemory() / mb);
 
        //Print Maximum available memory
        System.out.println("Max Memory:" + runtime.maxMemory() / mb);
    }
	
	public Vector<HaarRect> Consolidation(Vector<HaarRect> rects){
		Vector<HaarRect> result = new Vector<HaarRect>();
		while (rects.size()>0) {
			HaarRect re0 = rects.get(0);
			rects.remove(0);
			double x=re0.x,y=re0.y,w=re0.width,h=re0.height;
			int cnt=1;
			for (int i=0; i<rects.size();i++) {
				HaarRect rec = rects.get(i);
				if (Evaluate(re0,rec)) {
					cnt++;
					x+=rec.x; y+=rec.y;w+=rec.width;h+=rec.height;
					rects.remove(i);
					i--;
				}
			}
			HaarRect hr = new HaarRect((int)Math.round(x/cnt),(int)Math.round(y/cnt),(int)Math.round(w/cnt),(int)Math.round(h/cnt),cnt);
			result.add(hr);
		}
		rects.clear();
		rects = null;
		return result;
		
	}

	public int[][] 			CropFaces(int[][] img, HaarRect face){
		int[][] result = new int[face.width][face.height];
		for (int i=0;i<face.width;i++) {
			for (int j=0; j<face.height; j++) {
				result[i][j] = img[i+face.x][j+face.y];
			}
		}
		return result;
	}

	public BufferedImage	CropSubImage(String fName, HaarRect face) throws IOException{
        BufferedImage image = ImageIO.read(new File(fName));
        BufferedImage result= image.getSubimage(face.x, face.y, face.width,  face.height);
        image.flush();
        image=null;
		return result;
	}

	public BufferedImage	CropSubImage(String fName, HaarRect face, String sFile, double scale) throws IOException{
		face.x=face.x-(int)Math.floor((scale-1)/2*face.width);
		face.y=face.y-(int)Math.floor((scale-1)/2*face.height);
		face.height = (int)Math.floor(face.height*scale);
		face.width = (int)Math.floor(face.width*scale);
		
        BufferedImage image = ImageIO.read(new File(fName));
        BufferedImage result= image.getSubimage(face.x, face.y, face.width,  face.height);
        image.flush();
        image=null;
        File outputfile = new File(sFile);
        ImageIO.write(result, "jpg", outputfile);
		return result;
	}

	public void 			Destory() {
		idata.IImg=null;
		idata.VImg=null;
		idata =null;
		hc.stages=null;
		hc=null;
	}

	public void 			DrawFaceOnImage(Vector<HaarFace>  result,String fName,Color fColor, Color elColor, Color erColor, Color mColor, Color nColor) throws IOException {
		DrawFaceOnImage(searchFile, result,fName,fColor, elColor, erColor, mColor, nColor);
	}
	
	public void 			DrawFaceOnImage(String orgFile, Vector<HaarFace>  result,String fName,Color fColor, Color elColor, Color erColor, Color mColor, Color nColor) throws IOException {
        BufferedImage image = ImageIO.read(new File(orgFile));        
        Graphics2D g2d = image.createGraphics();
        BasicStroke bs = new BasicStroke(2);
        g2d.setStroke(bs);
        for (int i=0; i<result.size();i++) {
        	HaarFace ff = result.get(i);
        	if (fColor != null) g2d.setColor(fColor);
        	g2d.drawRect(ff.FaceRect.x,ff.FaceRect.y,ff.FaceRect.width,ff.FaceRect.height);
        	if (ff.Eye_Left != null ) {
        		if (elColor != null) g2d.setColor(elColor);
       			g2d.drawRect(ff.Eye_Left.x,ff.Eye_Left.y,ff.Eye_Left.width,ff.Eye_Left.height);
        	}
        	if (ff.Eye_Right != null ) {
        		if (erColor != null) g2d.setColor(erColor);
       			g2d.drawRect(ff.Eye_Right.x,ff.Eye_Right.y,ff.Eye_Right.width,ff.Eye_Right.height);
        	}
        	if (ff.Mouth != null) {
        		if (mColor != null) g2d.setColor(mColor);
       			g2d.drawRect(ff.Mouth.x,ff.Mouth.y,ff.Mouth.width,ff.Mouth.height);
        	}
        	if (ff.Nose != null) {
        		if (nColor != null) g2d.setColor(nColor);
       			g2d.drawRect(ff.Nose.x,ff.Nose.y,ff.Nose.width,ff.Nose.height);
        	}
        }
        File outputfile = new File(fName);
        ImageIO.write(image, "jpg", outputfile);
	}
	
	public void 			DrawFaceOnImage(String fName,Color[] cols, Vector<HaarRect>... result) throws IOException {
        BufferedImage image = ImageIO.read(new File(searchFile));        
        Graphics2D g2d = image.createGraphics();
        BasicStroke bs = new BasicStroke(2);
        g2d.setStroke(bs);

        for (int j=0; j<result.length;j++) {
        	if (cols != null)
        		g2d.setColor(cols[j]);
        	for(int i=0; i<result[j].size();i++) {
        		HaarRect rc = result[j].get(i);
        		g2d.drawRect(rc.x,rc.y,rc.width,rc.height);
        	}
        }
        File outputfile = new File(fName);
        ImageIO.write(image, "png", outputfile);
	}
	
	public void 			DrawRectOnImage( String fName,Vector<HaarRect>... results) throws IOException {
        BufferedImage image = ImageIO.read(new File(searchFile));        
        Graphics2D g2d = image.createGraphics();
        for (int n=0;n<results.length;n++)
        {
        	Vector<HaarRect> result = results[n];
        	for(int i=0; i<result.size();i++) {
     	   		HaarRect rc = result.get(i);
     	   		g2d.drawRect(rc.x,rc.y,rc.width,rc.height);
        	}
        }
        g2d.setColor(Color.BLACK);
        BasicStroke bs = new BasicStroke(2);
        g2d.setStroke(bs);
        File outputfile = new File(fName);
        ImageIO.write(image, "jpg", outputfile);
	}
	
	public void				DrawPic(int[][] img, String fName) throws IOException {
		int w = img.length, h = img[0].length;
		BufferedImage bi = new BufferedImage( w, h, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = bi.getRaster(); 
        for (int i=0;i<w;i++){
        	int[] arr = img[i];
         	raster.setPixels(i,0,1,h,arr);
        } 
        File outputfile = new File(fName);
       
        ImageIO.write(bi, "jpg", outputfile);
	}
	
	public boolean 			Evaluate(HaarRect rect1, HaarRect rect2) {
		int x1=rect1.x+(int)rect1.width/2;
		int y1=rect1.y+(int)rect1.height/2;
		int x2=rect2.x+(int)rect2.width/2;
		int y2=rect2.y+(int)rect2.height/2;
		double dis=Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
		double pixs = (Math.sqrt(rect2.width*rect2.height)+ Math.sqrt(rect1.width*rect1.height))/2;
		if (dis<pixs) {
			return true;
		}
		return false;
	}
	
	public int[][] 			GaussianBlurRaw(int [][] img) {
    	double[][] matrix = {{1.0/16,1.0/8,1.0/16},{1.0/8,1.0/4,1.0/8},{1.0/16,1.0/8,1.0/16}};   	
    	int SIZE=3;
        int height = img[0].length;
        int width = img.length;
        int[][] result = new int[width][height];
        for(int y = 0; y < height - 3; y++) {
            for(int x = 0; x < width - 3; x++) {
                double hsum=0;
            	for(int i = 0; i < SIZE; i++) {
                    for(int j = 0; j < SIZE; j++) {
                        hsum = hsum+(double)img[x + j][y + i] * matrix[i][j];
                    }
                }
                result[x+1][y+1]=(int)Math.round(hsum);
            }
        }
        // final image
        return result;
    }
	
	public static int[][]	HistogramEqualization(int[][] img){
		int w= img.length, h = img[0].length;
		int[] grays = new int[256], integral =new int[256], cdf=new int[256];
		for (int i=0;i<w; i++){
			for (int j=0; j<h; j++) {
				grays[img[i][j]]++;
			}
		}
		int min = -1, max =-1;
		int cnt=0, sum=0;
		for (int i=0;i<256; i++) {
			if (grays[i]!= 0) {
				if (min==-1) min = i;
				sum += grays[i];
				integral[i] = sum;
				max = i;
			}
		}
		double step = (double)255/(integral[max] - integral[min]);
		for (int i=min; i<=max; i++) {
			cdf[i]=(int)Math.round((integral[i]-integral[min])*step);
		}
		for (int i=0;i<w; i++){
			for (int j=0; j<h; j++) {
				img[i][j]=cdf[img[i][j]];
			}
		}
		return img;
	}
	
	public int[][] 			ReadImg(String fpath) throws IOException{
	   	BufferedImage img = null;
	    img = ImageIO.read(new File(fpath)); 
	    int width=img.getWidth();
	    int height = img.getHeight();
	   	int[][] image = new int[width][height];	 
 	    for (int i=0;i<width;i++) {
 	    	for (int j=0;j<height;j++) {
 	    		Color rgb= new Color(img.getRGB(i, j));
 	    		image[i][j]=(int)Math.round((double)rgb.getRed() * 0.2989 + (double)rgb.getGreen() * 0.587 + (double)rgb.getBlue() * 0.114);
 	    	}
 	    }
 	    img = null;
	    return image;

 	    //return GaussianBlurRaw(image);
	}
	
	public int[][] 			ReadGray(String fName){
        try {
            BufferedImage image = ImageIO.read(new File(fName));
            int w=image.getWidth(), h=image.getHeight();
            BufferedImage gray = new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
            // convert the original colored image to grayscale
            ColorConvertOp op = new ColorConvertOp(image.getColorModel().getColorSpace(),gray.getColorModel().getColorSpace(),null);
            op.filter(image,gray);
            Raster raster =gray.getData();
            int[][] array = new int[w][h];
            for (int j = 0; j < w; j++) {
                for (int k = 0; k < h; k++) {
                    array[j][k] = raster.getSample(j, k, 0);
                }
            }
            return array;
        } 
        catch (IOException ioe) {
        }
        return null;
	}
	
	public boolean 			RectOverlap(Vector<HaarRect> result, HaarRect rec) {
		for (int i=0; i<result.size();i++) {
			HaarRect rf = result.get(i);
			if (Evaluate(rf,rec)) return true;
		}
		return false;
	}
	
	public boolean 			RectOverlap(HaarRect rect1, HaarRect rect2) {
		if (rect1.x>rect2.x+rect2.width) return false;
		if (rect1.x+rect1.width<rect2.x) return false;
		if (rect1.y>rect2.y+rect2.height) return false;
		if (rect1.y+rect1.height<rect2.y) return false;
		return true;
	}
	
	public Vector<HaarRect>	SearchAreaHaarCascade(HaarRect sarea, HaarCascade s_hc,double stepScale){
		int iw = sarea.width;
		int ih = sarea.height;
		double sw = iw/s_hc.SizeX;
		double sh = ih/s_hc.SizeY;
		double ss=sw;
		if(sw < sh) ss =  sh;
		int totalSteps = (int)Math.floor(Math.log(ss)/Math.log(stepScale));
		Vector<HaarRect> result = new Vector<HaarRect>();
		for (int i=0;i<totalSteps;i++) {
			double scale = Math.pow(StepScale,i);
			SearchStagesCascade(sarea,s_hc,scale, result);
		}
		return Consolidation(result);
		//return result;
	}
	
	public void 			SearchHaarCascade(Vector<HaarRect> result){
		if (result==null) result= new Vector<HaarRect>();
		int iw = idata.IImg.length;
		int ih = idata.IImg[0].length;
		double SW = iw/hc.SizeX;
		double SH = ih/hc.SizeY;
		double SS=SW;
		if(SH < SH) SS =  SH;
		int totalSteps = (int)Math.floor(Math.log(SS)/Math.log(StepScale));
		//System.out.println("total steps: "+totalSteps);				
		//totalSteps=1;
		/*
		for (int i=totalSteps;i>=0;i--) {
			double scale = Math.pow(StepScale,i);
			SearchStagesCascade(scale, result);
		}		
		*/
		for (int i=0;i<totalSteps;i++) {
			double scale = Math.pow(StepScale,i);
			SearchStagesCascade(scale, result);
		}
				
	}
	
	public void 			SearchStagesCascade(double scale, Vector<HaarRect> result) {
		int stp=(int)Math.round(hc.SizeX*scale/10);
		int stpy=(int)Math.round(hc.SizeY*scale/10);
		int w =(int)Math.round(scale * hc.SizeX), h = (int)Math.round(scale * hc.SizeY);
		int i=0, j=0;
		while (i<idata.Width-w-1) {
			j=0;
			while (j<idata.Height-h-1) {
				HaarRect area = new HaarRect(i,j,w,h,scale);
				//if (!RectOverlap(result,area)) 
					SearchSingleArea(area,result);
				j+=stpy;
			}
			i+=stp;
			//System.out.println("i: "+i);
		}
	}
		
	public void 			SearchStagesCascade(HaarRect s_a, HaarCascade s_hc, double scale, Vector<HaarRect> result) {
		int stp=(int)Math.round(s_hc.SizeX*scale/10);
		int stpy=(int)Math.round(s_hc.SizeY*scale/10);

		int w =(int)Math.round(scale * s_hc.SizeX), h = (int)Math.round(scale * s_hc.SizeY);
		int i=0, j=0;
		while (i<s_a.width-w-1) {
			j=0;
			while (j<s_a.height-h-1) {
				HaarRect area = new HaarRect(s_a.x+i,s_a.y+j,w,h,scale);
				SearchSingleArea(area,s_hc,result);
				j+=stpy;
			}
			i+=stp;
		}
	}
	
	public void 			SearchSingleArea(HaarRect area, Vector<HaarRect> result) {
		double std = idata.StandardDeviation(area);
		for (int i=0;i<hc.stages.length;i++) {
			//System.out.println("stage# "+i);
			if (!idata.StageCheck(area, hc.stages[i],std)) return;
			if (maxStage < i) maxStage=i;
		}
		//int m_s = mouth.size(), n_s = nose.size(), e_s = eyes.size(); 
		//if (!(e_s==0 && m_s==0 && n_s==0)) {
		//	System.out.println("eyes: "+eyes.size()+" mouth: "+ mouth.size()+" nose: "+nose.size());
		//	result.add(area);
		//}
		result.add(area);

	}
	
	public void 			SearchSingleArea(HaarRect area, HaarCascade s_hc, Vector<HaarRect> result) {
		double std = idata.StandardDeviation(area);
		for (int i=0;i<s_hc.stages.length;i++) {
			//System.out.println("stage# "+i);
			if (!idata.StageCheck(area, s_hc.stages[i],std)) return;
			if (maxStage < i) maxStage=i;
		}
		//printRect(area);
		result.add(area);
	}
	
	public void				SortHaarRects(Vector<HaarRect> rects) {
		Collections.sort(rects, new Comparator<HaarRect>() {
	        @Override
	        public int compare(HaarRect o1, HaarRect o2) {
	          return (o1.weight>o2.weight)?-1:((o1.weight==o2.weight)?0:1);
	        }           
	    });

	}
	
	public void				SortHaarFaces(Vector<HaarRect> rects) {
		Collections.sort(rects, new Comparator<HaarRect>() {
	        @Override
	        public int compare(HaarRect o1, HaarRect o2) {
	        	int area1 =o1.height*o1.width;
	        	int area2 =o2.height*o2.width;
	          return (area1>area2)?-1:((area1==area2)?0:1);
	        }           
	    });

	}
	
	public int[][] 			SharpRaw(int [][] img) {
        int height = img[0].length;
        int width = img.length;
		int[][] gimg = GaussianBlurRaw(img);
		for (int i=0; i<width; i++) {
			for (int j=0; j<height; j++) {
				int hint = 2*img[i][j] - gimg[i][j];
				img[i][j] = hint<0?0:(hint>255?255:hint);
				
			}
		}
		img=HistogramEqualization(img);
        // final image
        return img;
    }

	

	public void 			printRect(HaarRect area) {
		System.out.println("rect "+area.x+","+area.y+","+area.width+","+area.height+" w: "+ area.weight);
	}

	public void 			printRect(Vector<HaarRect> areas) {
		for (int i=0;i<areas.size();i++) printRect(areas.get(i));
	}
}
