/*=====================================================================================*/
/*Project : 		LifeRecorder App
/*����\��G	�p��S�x
/*���p�ɮסG	FFT.java , Complex.java
/*=====================================================================================*/

package smatch.com.DC;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/** @description �q��J��ƭp��S�x�� */
public class Features {
	private List<Float> dataX;
	private List<Float> dataY;
	private List<Float> dataZ;
	private double[] featureSet = new double[26];
	
	public Features(List<Float> x, List<Float> y, List<Float> z) {
			this.dataX = middleList(x);
			this.dataY = middleList(y);
			this.dataZ = middleList(z);
	}
	
	/* �p���W��S�x�ɡA��140����ƪ�128���q */
	public List<Float> middleList(List<Float> list) {
  		List<Float> newlist = new ArrayList<Float>();
  		if(list.size()<=128){
  			return list;
  		}else{
  			for(int i=6;i<134;i++){
  	  			newlist.add(list.get(i));
  	  		}
  	  		return newlist;
  		}
  	}
	
	/* ��l������W��  */
	public Complex[] transform(List<Float> data) {
		Complex[] beforeTrans;
		Complex[] afterTrans;
		beforeTrans = new Complex[data.size()];
		for (int i = 0; i < data.size(); i++) {
			beforeTrans[i] = new Complex(data.get(i), 0);
		}
		afterTrans = FFT.fft(beforeTrans);
		return afterTrans;
	}
	
	public double[] getFeatureSet() {
		featureSet[0] = this.getAmplitude(dataX);
		featureSet[1] = this.getAmplitude(dataY);
		featureSet[2] = this.getAmplitude(dataZ);
		featureSet[3] = this.getCorrelation(dataX, dataY);
		featureSet[4] = this.getCorrelation(dataY, dataZ);
		featureSet[5] = this.getCorrelation(dataX, dataZ);
		featureSet[6] = this.getIQR(dataX);
		featureSet[7] = this.getIQR(dataY);
		featureSet[8] = this.getIQR(dataZ);
		featureSet[9] = this.getMean(dataX);
		featureSet[10] = this.getMean(dataY);
		featureSet[11] = this.getMean(dataZ);
		featureSet[12] = this.getMedian(dataX);     //�ƥ�
		featureSet[13] = this.getMedian(dataY);
		featureSet[14] = this.getMedian(dataZ);
		featureSet[15] = this.getStep(dataX);
		featureSet[16] = this.getStep(dataY);
		featureSet[17] = this.getStep(dataZ);
		featureSet[18] = this.getPowerSpectral(dataX);     //�ƥ�
		featureSet[19] = this.getPowerSpectral(dataY);
		featureSet[20] = this.getPowerSpectral(dataZ);     //�ƥ�
		featureSet[21] = this.getSma(dataX, dataY, dataZ);
		featureSet[22] = this.getSvm(dataX, dataY, dataZ);  
		featureSet[23] = this.getVariance(dataX);
		featureSet[24] = this.getVariance(dataY);
		featureSet[25] = this.getVariance(dataZ);
		return featureSet;
	}
	
	
	//���o�������T
	public double getAmplitude(List<Float> data) {
		//List<Float> data2 = middleList(data);
		double tmp = 0.0;
		Complex[] after = transform(data);
		for (int i = 0; i < after.length; i++)
		{
			tmp += after[i].abs();
		}
		return tmp / data.size();
	}
	
	
	//���o�����Y��
	public double getCorrelation(List<Float> data1, List<Float> data2) {
		double r, de;
		int indexX, n;
		double x_sum, y_sum, xx_sum, yy_sum, xy_sum;
		x_sum = y_sum = xx_sum = yy_sum = xy_sum = 0.0;
		
		n = data1.size();
		indexX = n-1;
		for(indexX=n-1;indexX>0;indexX--){
			x_sum += data1.get(indexX);
			y_sum += data2.get(indexX);
			xx_sum += data1.get(indexX)*data1.get(indexX);
			yy_sum += data2.get(indexX)*data2.get(indexX);
			xy_sum += data1.get(indexX)*data2.get(indexX);
		}
		if((Math.sqrt((n * xx_sum - x_sum * x_sum) * (n * yy_sum - y_sum * y_sum)))<0.000000001){
			de=0.000000001;
		}else{
			de=(Math.sqrt((n * xx_sum - x_sum * x_sum) * (n * yy_sum - y_sum * y_sum)));
		}
		r = ((n * xy_sum - x_sum * y_sum) / de);
		return r;
	}
	
	
	//���o�|����t
	public double getIQR(List<Float> data) {
		Float[] data2 = new Float[data.size()];
		int middle = data2.length / 2;
		data.toArray(data2);
		Arrays.sort(data2);
		Float[] Q1 = new Float[middle];
		Float[] Q2 = new Float[middle];
		for (int i = 0; i < middle; i++) {
			Q1[i] = data2[i];
			Q2[i] = data2[data2.length - i - 1];
		}
		return computeMedian(Q2) - computeMedian(Q1);
	}
	
	
	//���o������
	public double getMean(List<Float> data) {
		double i = 0.0;
		for (int j = 0; j < data.size(); j++)
		{
			i += data.get(j);
		}
		return i / data.size();
	}
	
	//���o�����
	public double getMedian(List<Float> data) {
		// sort
		Float[] data2 = new Float[data.size()];
		data.toArray(data2);
		Arrays.sort(data2);
		// median
		return computeMedian(data2);
	}
	
	//�p�⤤���
	public double computeMedian(Float[] data) {
		int middle = data.length / 2;
		if (data.length % 2 == 1) {
			return data[middle];
		} else {
			return (data[middle] + data[middle - 1]) / 2.0; 
		}
	}
	
	
	//���opeak��
	public double getStep(List<Float> data) {
		DataPreProcess dataPX = new DataPreProcess(data);
		List<Float> data2 = dataPX.getMOA();
		 double result = 0.0;
		 for (int i = 1; i < data2.size() - 1; i++) {
			 if(data2.get(i) - data2.get(i-1) > 0.3 && data2.get(i) - data2.get(i+1) > 0.3){
				 result++;
			 }
		 }
		 return result;
	}
	
	//���oSMA
	public double getSma(List<Float> data1, List<Float> data2, List<Float> data3) {
		double sumX = 0.0;
		double sumY = 0.0;
		double sumZ = 0.0;
		double sumAll = 0.0;
		for(int i = 0; i < data1.size(); i++) {
			sumX += Math.abs(data1.get(i));
			sumY += Math.abs(data2.get(i));
			sumZ += Math.abs(data3.get(i));
		}
		sumAll = sumX+sumY+sumZ;
		//return (1.0/data1.size()) * sumAll;
		return sumAll / data1.size();
	}
	
	
	//�T�b���������T
	public double getMeanAmp(List<Float> data1, List<Float> data2, List<Float> data3) {
		double tmp1 = 0.0;
		double tmp2 = 0.0;
		double tmp3 = 0.0;
		Complex[] after1 = transform(data1);
		Complex[] after2 = transform(data2);
		Complex[] after3 = transform(data3);
		for (int i = 0; i < after1.length; i++)
		{
			tmp1 += after1[i].abs();
			tmp2 += after2[i].abs();
			tmp3 += after3[i].abs();
		}
		return (tmp1 + tmp2 + tmp3) / data1.size();
	}
	
	
	
	
	
	//���o�зǮt
	public double getStd(List<Float> data) {
		return Math.sqrt(this.getVariance(data));
	}
	
	//���o�ܲ���
	public double getVariance(List<Float> data) {
		double result = 0.0;
		double x = 0.0;
		for (int i = 0; i < data.size(); i++ )
		{
			x += Math.pow(data.get(i), 2);
		}
		result = (x - data.size() * Math.pow(getMean(data), 2)) / (data.size() - 1);
		return result;
	}
	
	//���oSVM
	public double getSvm(List<Float> data1, List<Float> data2, List<Float> data3) {
		double svm=0.0;
		int i,n;
		double xx, yy, zz, sum;
		xx = yy = zz = sum = 0.0;
		
		n = data1.size();
		for(i=0;i<n;i++){
			xx = data1.get(i)*data1.get(i);
			yy = data2.get(i)*data2.get(i);
			zz = data3.get(i)*data3.get(i);
			sum+=Math.sqrt(xx+yy+zz);
		}
		svm=sum/n;
		return svm;
	}
	
	//���oPower Spectral
	public double getPowerSpectral(List<Float> data) {
		// �N�˥��I��֨�128�I�A�H�Q�󰵳ť߸��ഫ
		//List<Float> data2 = middleList(data);
		double tmp = 0.0;
		Complex[] after = transform(data);
		for (int i = 0; i < after.length; i++)
		{
			tmp += Math.pow(after[i].abs(), 2);
		}
		return tmp / data.size();
	}
	
}
