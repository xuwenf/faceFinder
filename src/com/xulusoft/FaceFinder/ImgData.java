package com.xulusoft.FaceFinder;

import java.io.IOException;

import com.xulusoft.FaceFinder.HaarCascade.Stage;
import com.xulusoft.FaceFinder.HaarCascade.Tree;

public class 			ImgData{
	public int Width=0;
	public int Height=0;
	public int[][] IImg;
	public int[][] VImg;
	public ImgData(int[][] img) throws IOException {
		Width = img.length;
		Height = img[0].length;
		//PrintImg(img,"orig",5,5);
		IImg= IntegralImage(img);
		//PrintImg(IImg,"intergral",5,5);
		VImg = IntegralImage(img,true);
		//PrintImg(VImg,"variance",5,5);
		//FaceSearch.checkMemory();
	}
	
	public void 		PrintImg(int[][] img, String name, int x, int y) {
		System.out.println(name);
		for (int i=0;i<x;i++) {
			for (int j=0;j<y;j++) {
				System.out.print(img[i][j]+",\t");
			}
			System.out.println();
		}
	}
	
	public int 			RectSum(int[][] img, HaarRect rec) {
		int rb = img[rec.x+rec.width][rec.y+rec.height];
		int lt = img[rec.x][rec.y];	
		int lb = img[rec.x][rec.y+rec.height];
		int rt = img[rec.x+rec.width][rec.y];
		return rb +lt - lb - rt;
	}
			
	public double 		StandardDeviation(HaarRect rec) {
		double invArea = 1/((double)rec.width*(double)rec.height);
		double mean = RectSum(IImg,rec)*invArea;
		double variance=0;
		variance = RectSum(VImg,rec);
		variance = variance * invArea - mean*mean;
		return (variance<0)?1:Math.sqrt(variance);
	}
	
	public boolean 		StageCheck(HaarRect area, Stage stage, double std) {
		double rs=0;
		for (int i=0; i<stage.trees.size();i++) {
			rs += TreeValue(area,stage.trees.get(i),std);
		}
		return (rs>=stage.threshold);
	}
	
	public double 		TreeValue(HaarRect area, Tree tree, double std) {
		double scale = area.weight;
		double rs=0;
		HaarRect crec = new HaarRect();
		//System.out.println("searchArea "+area.x+","+area.y+","+area.width+","+area.height);
		for (int i=0; i<tree.feature.rects.size();i++) {
			//System.out.println("rect : "+i);
			HaarRect rec=tree.feature.rects.get(i);
			//System.out.println("in: "+rec.x+","+rec.y+","+rec.width+","+rec.height+","+rec.weight);

			crec.x = (int)Math.round(scale*rec.x) + area.x;
			crec.y = (int)Math.round(scale*rec.y) + area.y;
			crec.width = (int)Math.round(scale*rec.width);
			crec.height = (int)Math.round(scale*rec.height);
			rs += RectSum(IImg,crec)*rec.weight;
		}
		rs= rs/(double)area.width/(double)area.height/std;
		//System.out.println("tree sum: "+rs+" tree thres: "+tree.threshold);
		return (rs>=tree.threshold)?tree.right_val:tree.left_val;
	}
	
	public int[][] 		IntegralImage(int[][] img){
		int w=img.length;
		int h = img[0].length;
		int[][] result= new int[w][h];
		for (int i=0; i<w;i++) {
			for (int j=0; j<h;j++) {
				int sum=0;
				for (int k=0; k<j;k++) sum += img[i][k];
				for (int k=0; k<i;k++) sum += img[k][j];
				result[i][j]=sum + img[i][j]+((i>0 && j>0)?result[i-1][j-1]:0);
			}
		}
		
		return result;
	}
	
	public int[][] 		IntegralImage(int[][] img, boolean variance) {
		int w=img.length;
		int h = img[0].length;
		int[][] vimg= new int[w][h];
		for (int i=0; i<w; i++) {
			for (int j=0; j<h; j++) {
				vimg[i][j]=img[i][j]*img[i][j];
			}
		}
		return IntegralImage(vimg);
	}
	
}
