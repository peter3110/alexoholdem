package ao.bucket.abstraction.bucketize.smart;

import ao.bucket.abstraction.access.tree.BucketTree;
import ao.bucket.abstraction.bucketize.Bucketizer;
import ao.bucket.abstraction.bucketize.linear.IndexedStrengthList;
import ao.util.math.rand.MersenneTwisterFast;
import ao.util.time.Stopwatch;
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * User: Alex Ostrovsky
 * Date: 12-May-2009
 * Time: 8:51:59 PM
 */
public class KMeansBucketizer implements Bucketizer
{
    //--------------------------------------------------------------------
    private static final Logger LOG =
            Logger.getLogger(KMeansBucketizer.class);

    private static final double DELTA_CUTOFF = 0.01;


    //--------------------------------------------------------------------
    public boolean bucketize(
            BucketTree.Branch branch,
            byte              numBuckets)
    {
        Stopwatch           time      = new Stopwatch();
        IndexedStrengthList strengths =
                IndexedStrengthList.strengths(branch);
        double              means[]   = initMeans(strengths, numBuckets);

        int  counts  [] = new int[numBuckets];
        byte clusters[] = cluster(means, strengths);
        for (int i = 0; i < clusters.length; i++) {
            branch.set(strengths.index(i),
                       clusters[i]);

            counts[ clusters[i] ]++;
        }

        LOG.debug("bucketized " + branch.round() +
                  " into " + numBuckets +
                  "\t(p " + branch.parentCanons().length +
                  " \tc " + strengths.length()   +
                  ")\t" + Arrays.toString(counts) +
                  "\ttook " + time);

        return true;
    }


    //--------------------------------------------------------------------
    private byte[] cluster(
            double              means[],
            IndexedStrengthList strengths)
    {
        byte clusters[] = new byte[ strengths.length() ];

        double delta;
        do
        {
            delta = iterateKMeans(means, strengths, clusters);
        }
        while (delta > DELTA_CUTOFF);

        return clusters;
    }

    // see http://en.wikipedia.org/wiki/K-means
    private double iterateKMeans(
            double              means[],
            IndexedStrengthList strengths,
            byte                clusters[])
    {
        assignmentStep(means, strengths, clusters);
        return updateStep(means, strengths, clusters);
    }


    //--------------------------------------------------------------------
    // Assign each observation to the cluster with the closest mean
    //  (i.e. partition the observations according to the
    //          Voronoi diagram generated by the means).
    private void assignmentStep(
            double              means[],
            IndexedStrengthList strengths,
            byte                clusters[])
    {
        for (int i = 0; i < strengths.length(); i++) {
            double strength = strengths.strength(i);

            byte   leastDistIndex = -1;
            double leastDistance  = Double.POSITIVE_INFINITY;
            for (byte j = 0; j < means.length; j++) {

                double distance = Math.abs(strength - means[j]);
                if (leastDistance > distance) {
                    leastDistance  = distance;
                    leastDistIndex = j;
                }
            }

            clusters[ i ] = leastDistIndex;
        }
    }

    
    //--------------------------------------------------------------------
    // Calculate the new means to be the centroid of the
    //   observations in the cluster.
    private double updateStep(
            double              means[],
            IndexedStrengthList strengths,
            byte                clusters[])
    {
        double maxDelta = 0;
        for (int i = 0; i < means.length; i++) {

            double sum   = 0;
            int    count = 0;

            for (int j = 0; j < clusters.length; j++) {
                if (clusters[j] != i) continue;

                sum += strengths.strength(j);
                count++;
            }

            double newMean = sum / count;
            double delta   = Math.abs(newMean - means[i]);
            if (maxDelta < delta) {
                maxDelta = delta;
            }
            means[i] = newMean;
        }
        return maxDelta;
    }


    //--------------------------------------------------------------------
    /*
     * see
     *   http://en.wikipedia.org/wiki/K-means%2B%2B
     */
    private double[] initMeans(
            IndexedStrengthList details, byte nBuckets)
    {
        int means[] = new int[ nBuckets ];

        MersenneTwisterFast rand =
                new MersenneTwisterFast(details.length() * nBuckets);

        // Choose one center uniformly at random
        //  from among the data points.
        means[0] = rand.nextInt(details.length());

        for (int k = 1; k < nBuckets; k++)
        {
            // For each data point x, compute D(x), the distance between
            //   x and the nearest center that has already been chosen.

            double maxChance      = Double.NEGATIVE_INFINITY;
            int    maxChanceIndex = -1;

            next_point:
            for (int i = 0; i < details.length(); i++) {

                double nearestCluster = Double.POSITIVE_INFINITY;
                for (int j = 0; j < k; j++) {
                    if (i == means[j]) continue next_point;

                    double dist = Math.abs(details.strength(i) -
                                           details.strength(means[j]));
                    if (nearestCluster > dist) {
                        nearestCluster = dist;
                    }
                }

                // Each point x is chosen with
                //  probability proportional to D(x)^2.
                double chance = rand.nextDouble()
                        * nearestCluster * nearestCluster;
                if (maxChance < chance) {
                    maxChance      = chance;
                    maxChanceIndex = i;
                }
            }

            // Add one new data point as a center.
            means[k] = maxChanceIndex;
        }

        double meanVals[] = new double[ means.length ];
        for (int i = 0; i < means.length; i++) {
            meanVals[ i ] = details.strength( means[i] );
        }
        return meanVals;
    }


    //--------------------------------------------------------------------
    public String id()
    {
        return "kmeans";
    }
}
