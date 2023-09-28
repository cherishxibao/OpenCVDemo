package com.demo.myopencv.utils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageSimilarity {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static double cmpHash(String hash1, String hash2) {
        int n = 0;
        if (hash1.length() != hash2.length()) {
            return -1; // Hash lengths should be the same for comparison
        }
        for (int i = 0; i < hash1.length(); i++) {
            if (hash1.charAt(i) != hash2.charAt(i)) {
                n++;
            }
        }
        return 1 - (double) n / (64);
    }

    public static String pHash(String imgPath) {
        Mat img = Imgcodecs.imread(imgPath, Imgcodecs.IMREAD_GRAYSCALE);
        Mat resizedImg = new Mat();
        Imgproc.resize(img, resizedImg, new Size(64, 64), 0, 0, Imgproc.INTER_CUBIC);
        Mat dctImg = new Mat();
        resizedImg.convertTo(dctImg, CvType.CV_32F);
        Core.dct(dctImg, dctImg);
        dctImg = dctImg.submat(0, 32, 0, 32);

        StringBuilder hash = new StringBuilder();
        for (int i = 0; i < dctImg.rows(); i++) {
            for (int j = 0; j < dctImg.cols(); j++) {
                hash.append(dctImg.get(i, j)[0] >= 0 ? '1' : '0');
            }
        }
        return hash.toString();
    }

    public static void main(String[] args) {
        String path = "D:/data/similar/";
        String a = path + "0720_090352_840781.jpg";
        String b = path + "0720_133954_832176.jpg";

        String hash1 = pHash(a);
        String hash2 = pHash(b);

        double similarity = cmpHash(hash1, hash2);
        System.out.println("Average Hash algorithm similarity: " + similarity);
    }
}
