package magic;

import java.util.Random;

public class MagicClass {

	public static void main(String[] args) {
		MatrixPic picture = new MatrixPic(5,5,2);
		picture.setPredefinedPic();
		picture.selectAreaAroundPixel(new Random().nextInt(5), new Random().nextInt(5));
		picture.printCompareInt();
	}

}
