package edu.neu.ccs.pyramid.multilabel_classification.crf;

import edu.neu.ccs.pyramid.configuration.Config;
import edu.neu.ccs.pyramid.dataset.DataSetType;
import edu.neu.ccs.pyramid.dataset.MultiLabel;
import edu.neu.ccs.pyramid.dataset.MultiLabelClfDataSet;
import edu.neu.ccs.pyramid.dataset.TRECFormat;
import edu.neu.ccs.pyramid.eval.Accuracy;
import edu.neu.ccs.pyramid.eval.Overlap;
import edu.neu.ccs.pyramid.optimization.GradientDescent;
import edu.neu.ccs.pyramid.optimization.LBFGS;
import edu.neu.ccs.pyramid.optimization.Optimizer;

import java.io.File;

/**
 * Created by Rainicy on 12/14/15.
 */
public class CMLCRFTest {
    private static final Config config = new Config("/Users/Rainicy/Datasets/2.config");
    private static final String DATASETS = config.getString("input.datasets");
    public static void main(String[] args) throws Exception{
        test1();
    }

    private static void test1() throws Exception{
        MultiLabelClfDataSet dataSet = TRECFormat.loadMultiLabelClfDataSet(new File(DATASETS, "data_sets/train.trec"),
                DataSetType.ML_CLF_SPARSE, true);
        MultiLabelClfDataSet testSet = TRECFormat.loadMultiLabelClfDataSet(new File(DATASETS, "data_sets/test.trec"),
                DataSetType.ML_CLF_SPARSE, true);

        CMLCRF cmlcrf = new CMLCRF(dataSet);
        CRFLoss crfLoss = new CRFLoss(cmlcrf,dataSet,10000);


        MultiLabel[] predTrain;
        MultiLabel[] predTest;

        GradientDescent optimizer = new GradientDescent(crfLoss);
        for (int i=0; i<5000; i++) {
            optimizer.iterate();
            predTrain = cmlcrf.predict(dataSet);
            predTest = cmlcrf.predict(testSet);
//            System.out.print("Obj: " + optimizer.getTerminator().getLastValue());
            System.out.print("iter: "+ i);
            System.out.print("\tTrain acc: " + Accuracy.accuracy(dataSet.getMultiLabels(), predTrain));
            System.out.print("\tTrain overlap " + Overlap.overlap(dataSet.getMultiLabels(), predTrain));
            System.out.print("\tTest acc: " + Accuracy.accuracy(testSet.getMultiLabels(), predTest));
            System.out.println("\tTest overlap " + Overlap.overlap(testSet.getMultiLabels(), predTest));
        }



//        LBFGS optimizer = new LBFGS(crfLoss);
//        optimizer.getTerminator().setAbsoluteEpsilon(0.1);
//        for (int i=0; i<5000; i++) {
//            optimizer.iterate();
//            predTrain = cmlcrf.predict(dataSet);
//            predTest = cmlcrf.predict(testSet);
//            System.out.print("iter: "+ i);
//            System.out.print("\tTrain acc: " + Accuracy.accuracy(dataSet.getMultiLabels(), predTrain));
//            System.out.print("\tTrain overlap " + Overlap.overlap(dataSet.getMultiLabels(), predTrain));
//            System.out.print("\tTest acc: " + Accuracy.accuracy(testSet.getMultiLabels(), predTest));
//            System.out.println("\tTest overlap " + Overlap.overlap(testSet.getMultiLabels(), predTest));
//        }

        System.out.println(cmlcrf.getWeights());
    }
}
