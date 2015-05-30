package edu.neu.ccs.pyramid.experiment;

import edu.neu.ccs.pyramid.classification.boosting.lktb.LKTBConfig;
import edu.neu.ccs.pyramid.classification.boosting.lktb.LKTBTrainer;
import edu.neu.ccs.pyramid.classification.boosting.lktb.LKTreeBoost;
import edu.neu.ccs.pyramid.configuration.Config;
import edu.neu.ccs.pyramid.dataset.ClfDataSet;
import edu.neu.ccs.pyramid.dataset.DataSetType;
import edu.neu.ccs.pyramid.dataset.GradientMatrix;
import edu.neu.ccs.pyramid.dataset.TRECFormat;
import edu.neu.ccs.pyramid.eval.Accuracy;
import edu.neu.ccs.pyramid.regression.Regressor;
import edu.neu.ccs.pyramid.regression.probabilistic_regression_tree.ProbRegStump;
import edu.neu.ccs.pyramid.regression.probabilistic_regression_tree.ProbRegStumpTrainer;
import edu.neu.ccs.pyramid.regression.probabilistic_regression_tree.Sigmoid;
import edu.neu.ccs.pyramid.regression.regression_tree.LeafOutputType;
import edu.neu.ccs.pyramid.regression.regression_tree.RegressionTree;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * hard tree vs expectation tree vs hybrid tree, accuracy test
 * Created by chengli on 5/30/15.
 */
public class Exp115 {
    private static final Config config = new Config("configs/local.config");
    private static final String DATASETS = config.getString("input.datasets");
    private static final String TMP = config.getString("output.tmp");

    public static void main(String[] args) throws Exception{
        File folder = new File(TMP);
        FileUtils.cleanDirectory(folder);
        train_hard();
        train_expectation();
        train_hybrid();
    }

    static void train_hard() throws Exception{
        File folder = new File(TMP,"hard_tree");
        folder.mkdirs();
        ClfDataSet dataSet = TRECFormat.loadClfDataSet(new File(DATASETS, "/spam/trec_data/train.trec"),
                DataSetType.CLF_SPARSE, true);
        System.out.println(dataSet.getMetaInfo());
        ClfDataSet testSet = TRECFormat.loadClfDataSet(new File(DATASETS, "/spam/trec_data/test.trec"),
                DataSetType.CLF_SPARSE, true);

        LKTreeBoost lkTreeBoost = new LKTreeBoost(2);

        LKTBConfig trainConfig = new LKTBConfig.Builder(dataSet)
                .numLeaves(2).learningRate(1).numSplitIntervals(50).minDataPerLeaf(1)
                .dataSamplingRate(1).featureSamplingRate(1)
                .randomLevel(1)
                .setLeafOutputType(LeafOutputType.AVERAGE)
                .considerHardTree(true)
                .considerExpectationTree(false)
                .considerProbabilisticTree(false)
                .build();

        LKTBTrainer lktbTrainer = new LKTBTrainer(trainConfig,lkTreeBoost);

        File trainFile = new File(folder,"train_acc");
        File testFile = new File(folder,"test_acc");
        File typeFile = new File(folder,"type");

        for (int i=0;i<100;i++){
            System.out.println("iteration "+i);
            System.out.println("boosting accuracy = "+ Accuracy.accuracy(lkTreeBoost, dataSet));

            lktbTrainer.iterate();


            FileUtils.writeStringToFile(trainFile,""+Accuracy.accuracy(lkTreeBoost, dataSet)+"\n",true);
            FileUtils.writeStringToFile(testFile,""+Accuracy.accuracy(lkTreeBoost, testSet)+"\n",true);
            List<Regressor> regressors = lkTreeBoost.getRegressors(0);
            Regressor regressor = regressors.get(i);
            if (regressor instanceof RegressionTree){
                FileUtils.writeStringToFile(typeFile,"hard tree"+"\n",true);

            }
            if (regressor instanceof ProbRegStump){
                ProbRegStump probRegStump = (ProbRegStump)regressor;
                if (probRegStump.getLossType()== ProbRegStumpTrainer.LossType.SquaredLossOfExpectation){
                    FileUtils.writeStringToFile(typeFile,"expectation tree"+"\n",true);
                }
                if (probRegStump.getLossType()== ProbRegStumpTrainer.LossType.ExpectationOfSquaredLoss){
                    FileUtils.writeStringToFile(typeFile,"probabilistic tree"+"\n",true);
                }
            }
        }

    }


    static void train_hybrid() throws Exception{
        File folder = new File(TMP,"hybrid_tree");
        folder.mkdirs();
        ClfDataSet dataSet = TRECFormat.loadClfDataSet(new File(DATASETS, "/spam/trec_data/train.trec"),
                DataSetType.CLF_SPARSE, true);
        System.out.println(dataSet.getMetaInfo());
        ClfDataSet testSet = TRECFormat.loadClfDataSet(new File(DATASETS, "/spam/trec_data/test.trec"),
                DataSetType.CLF_SPARSE, true);

        LKTreeBoost lkTreeBoost = new LKTreeBoost(2);

        LKTBConfig trainConfig = new LKTBConfig.Builder(dataSet)
                .numLeaves(2).learningRate(1).numSplitIntervals(50).minDataPerLeaf(1)
                .dataSamplingRate(1).featureSamplingRate(1)
                .randomLevel(1)
                .setLeafOutputType(LeafOutputType.AVERAGE)
                .considerHardTree(true)
                .considerExpectationTree(true)
                .considerProbabilisticTree(false)
                .build();

        LKTBTrainer lktbTrainer = new LKTBTrainer(trainConfig,lkTreeBoost);

        File trainFile = new File(folder,"train_acc");
        File testFile = new File(folder,"test_acc");
        File typeFile = new File(folder,"type");

        for (int i=0;i<100;i++){
            System.out.println("iteration "+i);
            System.out.println("boosting accuracy = "+ Accuracy.accuracy(lkTreeBoost, dataSet));

            lktbTrainer.iterate();


            FileUtils.writeStringToFile(trainFile,""+Accuracy.accuracy(lkTreeBoost, dataSet)+"\n",true);
            FileUtils.writeStringToFile(testFile,""+Accuracy.accuracy(lkTreeBoost, testSet)+"\n",true);
            List<Regressor> regressors = lkTreeBoost.getRegressors(0);
            Regressor regressor = regressors.get(i);
            if (regressor instanceof RegressionTree){
                FileUtils.writeStringToFile(typeFile,"hard tree"+", ",true);

            }
            if (regressor instanceof ProbRegStump){
                ProbRegStump probRegStump = (ProbRegStump)regressor;
                if (probRegStump.getLossType()== ProbRegStumpTrainer.LossType.SquaredLossOfExpectation){
                    FileUtils.writeStringToFile(typeFile,"expectation tree"+", ",true);
                }
                if (probRegStump.getLossType()== ProbRegStumpTrainer.LossType.ExpectationOfSquaredLoss){
                    FileUtils.writeStringToFile(typeFile,"probabilistic tree"+", ",true);
                }
            }
        }

    }

    static void train_expectation() throws Exception{
        File folder = new File(TMP,"expectation_tree");
        folder.mkdirs();
        ClfDataSet dataSet = TRECFormat.loadClfDataSet(new File(DATASETS, "/spam/trec_data/train.trec"),
                DataSetType.CLF_SPARSE, true);
        System.out.println(dataSet.getMetaInfo());
        ClfDataSet testSet = TRECFormat.loadClfDataSet(new File(DATASETS, "/spam/trec_data/test.trec"),
                DataSetType.CLF_SPARSE, true);

        LKTreeBoost lkTreeBoost = new LKTreeBoost(2);

        LKTBConfig trainConfig = new LKTBConfig.Builder(dataSet)
                .numLeaves(2).learningRate(1).numSplitIntervals(50).minDataPerLeaf(1)
                .dataSamplingRate(1).featureSamplingRate(1)
                .randomLevel(1)
                .setLeafOutputType(LeafOutputType.AVERAGE)
                .considerHardTree(false)
                .considerExpectationTree(true)
                .considerProbabilisticTree(false)
                .build();

        LKTBTrainer lktbTrainer = new LKTBTrainer(trainConfig,lkTreeBoost);

        File trainFile = new File(folder,"train_acc");
        File testFile = new File(folder,"test_acc");
        File typeFile = new File(folder,"type");

        for (int i=0;i<100;i++){
            System.out.println("iteration "+i);
            System.out.println("boosting accuracy = "+ Accuracy.accuracy(lkTreeBoost, dataSet));

            lktbTrainer.iterate();


            FileUtils.writeStringToFile(trainFile,""+Accuracy.accuracy(lkTreeBoost, dataSet)+"\n",true);
            FileUtils.writeStringToFile(testFile,""+Accuracy.accuracy(lkTreeBoost, testSet)+"\n",true);
            List<Regressor> regressors = lkTreeBoost.getRegressors(0);
            Regressor regressor = regressors.get(i);
            if (regressor instanceof RegressionTree){
                FileUtils.writeStringToFile(typeFile,"hard tree"+"\n",true);

            }
            if (regressor instanceof ProbRegStump){
                ProbRegStump probRegStump = (ProbRegStump)regressor;
                if (probRegStump.getLossType()== ProbRegStumpTrainer.LossType.SquaredLossOfExpectation){
                    FileUtils.writeStringToFile(typeFile,"expectation tree"+"\n",true);
                }
                if (probRegStump.getLossType()== ProbRegStumpTrainer.LossType.ExpectationOfSquaredLoss){
                    FileUtils.writeStringToFile(typeFile,"probabilistic tree"+"\n",true);
                }
            }
        }

    }
}