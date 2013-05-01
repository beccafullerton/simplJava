package simpl.descriptions.indexers;

import java.util.List;

import simpl.core.indexers.ItemIndexPredicate;
import simpl.core.indexers.MultiIndexer;
import simpl.descriptions.FieldDescriptor;
import simpl.descriptions.FieldType;

import java.util.List;
import java.util.LinkedList;;

public class FieldDescriptorIndexer extends MultiIndexer<FieldDescriptor>
{
	public InnerIndexer<FieldDescriptor> Scalars;
	public InnerIndexer<FieldDescriptor> ScalarCollections;
	public InnerIndexer<FieldDescriptor> ScalarMaps;
	public InnerIndexer<FieldDescriptor> Composites;
	public InnerIndexer<FieldDescriptor> CompositeCollections;
	public InnerIndexer<FieldDescriptor> CompositeMaps;
	public InnerIndexer<FieldDescriptor> CompositesAsScalars; 
	public InnerIndexer<FieldDescriptor> IgnoredElements;
	public InnerIndexer<FieldDescriptor> IgnoredAttributes;
	
	final class byScalars extends FieldTypeItemPredicate{
		@Override
		public FieldType getFieldType() {
			// TODO Auto-generated method stub
			return FieldType.SCALAR;
		}
	}
	
	final class byScalarCollections extends FieldTypeItemPredicate{

		@Override
		public FieldType getFieldType() {
			// TODO Auto-generated method stub
			return FieldType.COLLECTION_SCALAR;
		}
	}
	
	final class byScalarMaps extends FieldTypeItemPredicate{

		@Override
		public FieldType getFieldType() {
			// TODO Auto-generated method stub
			return FieldType.MAP_SCALAR;
		}
		
	}
	
	final class byComposites extends FieldTypeItemPredicate{

		@Override
		public FieldType getFieldType() {
			// TODO Auto-generated method stub
			return FieldType.COMPOSITE_ELEMENT;
		}
		
	}
	
	final class byCompositeCollections extends FieldTypeItemPredicate{

		@Override
		public FieldType getFieldType() {
			// TODO Auto-generated method stub
			return FieldType.COLLECTION_ELEMENT;
		}
		
	}
	
	final class byCompositeMaps extends FieldTypeItemPredicate{

		@Override
		public FieldType getFieldType() {
			// TODO Auto-generated method stub
			return FieldType.MAP_ELEMENT;
		}
		
	}
	
	final class byIgnoredAttribute extends FieldTypeItemPredicate{
		@Override
		public FieldType getFieldType(){
			return FieldType.IGNORED_ATTRIBUTE;
		}
	}
	
	final class byIgnoredElement extends FieldTypeItemPredicate{
		@Override
		public FieldType getFieldType(){
			return FieldType.IGNORED_ELEMENT;
		}
	}
	
	final class byCompositeAsScalar extends ItemIndexPredicate<FieldDescriptor>
	{

		@Override
		public String GetIndexIdentifier() {
			// TODO Auto-generated method stub
			return "compositeasscalar";
		}

		@Override
		public String ObtainIndex(FieldDescriptor item) {
			// TODO Auto-generated method stub
			if(item.containsMetaInformation("simpl_composite_as_scalar"))
			{
				return item.getName();
			}
			else
			{
				return "";//Returning an empty string means it won't be indexed. 
				// Thus byCompositeAsScalar will only contain the one field that is
				// composite as scalar! :D 
			}
		}
		
	}
	
	// -- here are the other indexers, by tag name, etc. 
	
	
	final class byName extends ItemIndexPredicate<FieldDescriptor>
	{

		@Override
		public String GetIndexIdentifier() {
			// TODO Auto-generated method stub
			return "name";
		}

		@Override
		public String ObtainIndex(FieldDescriptor item) {
			// TODO Auto-generated method stub
			return item.getName();
		}
		
	}
	
	public FieldDescriptorIndexer()
	{
	// These are all of the type indexers... for use in the de/serialization process.
		this.ScalarCollections = this.by(new byScalarCollections());
		this.Scalars = this.by(new byScalars());
		this.ScalarMaps = this.by(new byScalarMaps());
		this.Composites = this.by(new byComposites());
		this.CompositeCollections = this.by(new byCompositeCollections());
		this.CompositeMaps = this.by(new byCompositeMaps());
		this.IgnoredAttributes = this.by(new byIgnoredAttribute());
		this.IgnoredElements = this.by(new byIgnoredElement());	
		this.CompositesAsScalars = this.by(new byCompositeAsScalar());
	}
	
	@Override
	public List<ItemIndexPredicate<FieldDescriptor>> getIndexPredicates() {
		// TODO Auto-generated method stub
		List<ItemIndexPredicate<FieldDescriptor>> ourList = new LinkedList<ItemIndexPredicate<FieldDescriptor>>();
		ourList.add(new byScalars());
		ourList.add(new byScalarCollections());
		ourList.add(new byScalarMaps());
		ourList.add(new byComposites());
		ourList.add(new byCompositeCollections());
		ourList.add(new byCompositeMaps());
		ourList.add(new byIgnoredAttribute());
		ourList.add(new byIgnoredElement());
		ourList.add(new byName());
		ourList.add(new byCompositeAsScalar());
		return ourList;
	}
}
