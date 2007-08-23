package ao.decision;

import ao.decision.attr.Attribute;
import ao.decision.attr.AttributeSet;
import ao.decision.data.Context;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class DecisionTree<A, T>
{
    //--------------------------------------------------------------------
    private Map<Attribute<?>, DecisionTree<?, T>> nodes;
    private AttributeSet<?>                       attrSet;


    //--------------------------------------------------------------------
    public DecisionTree(AttributeSet<?> attributeSet)
    {
        nodes   = new HashMap<Attribute<?>, DecisionTree<?,T>>();
        attrSet = attributeSet;
    }


    //--------------------------------------------------------------------
    public void addNode(Attribute<?>       attribute,
                        DecisionTree<?, T> tree)
    {
        assert !nodes.containsKey( attribute );
        nodes.put(attribute, tree);
    }


    //--------------------------------------------------------------------
    // minumum number of bits needed to encode the graph.
    public double codingComplexity()
    {
        return 0;
    }


    //--------------------------------------------------------------------
    public Histogram<T> predict(Context basedOn)
    {
        Attribute<?>       attribute = basedOn.attribute(attrSet);
        DecisionTree<?, T> subTree   = nodes.get( attribute );
        if (subTree == null)
        {
            // predicting based on an attribute that was never seen before
            //  assume its the same as an arbitrary seen attribute.
            subTree = nodes.values().iterator().next();
        }
        return subTree.predict( basedOn );
    }
}