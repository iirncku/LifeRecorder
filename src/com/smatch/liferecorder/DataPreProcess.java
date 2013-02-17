/*=====================================================================================*/
/*Project : 		LifeRecorder App
/*����\��G	��Data���o�i�B�z
/*=====================================================================================*/
package smatch.com.DC;
import java.util.ArrayList;
import java.util.List;
/** @description �s���o�i�������O */
public class DataPreProcess {
	private List<Float> rawData;
	public DataPreProcess(List<Float> data) {
		this.rawData = data;
	}
	/* �p��Peak�ɩһݪ��o�i�B�z�A�ĥ�Moving Average */
	public List<Float> movingAverage(List<Float> data) {
		List<Float> result = new ArrayList<Float>();
		for (int i = 0; i < data.size() - 4; i++) {
			float temp = 0;
			for (int j = i; j < i + 5; j++) {
				 temp += data.get(j);
			}
			result.add(temp / 5);
		}
		return result;
	}
	public List<Float> getMOA() {
		return movingAverage(rawData);
	}
}