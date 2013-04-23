package simpl.descriptions.generics.testclasses;

import java.util.List;

import simpl.annotations.dbal.simpl_collection;


public class Search<T extends SearchResult>
{

	@simpl_collection("result")
	List<T>	results;

}
