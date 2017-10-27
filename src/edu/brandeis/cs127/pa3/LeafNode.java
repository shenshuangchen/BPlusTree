package edu.brandeis.cs127.pa3;

/**
   LeafNodes of B+ trees
 */
public class LeafNode extends Node {
	private int[] tempKeys=null;
	/**
       Construct a LeafNode object and initialize it with the parameters.
       @param d the degree of the leafNode
       @param k the first key value of the node
       @param n the next node 
       @param p the previous node
	 */
	public LeafNode (int d, int k, Node n, Node p){
		super (d, n, p);
		keys [1] = k;
		lastindex = 1;
	}      


	public void outputForGraphviz() {

		// The name of a node will be its first key value
		// String name = "L" + String.valueOf(keys[1]);
		// name = BTree.nextNodeName();

		// Now, prepare the label string
		String label = "";
		for (int j = 0; j < lastindex; j++) {
			if (j > 0) label += "|";
			label += String.valueOf(keys[j+1]);
		}
		// Write out this node
		BTree.writeOut(myname + " [shape=record, label=\"" + label + "\"];\n");
	}

	/** 
	the minimum number of keys the leafnode should have.
	 */
	public int minkeys () {
		int min = 0;
		///////////////////
		//complete// ADD CODE HERE //
		///////////////////
		min = (degree)/2;
		//if the node is a root, the number is 1
		if(parentref==null){
			min = 1;
		}
		return min;
	}

	/**
       Check if this node can be combined with other into a new node without splitting.
       Return TRUE if this node and other can be combined. 
       @return true if this node can be combined with other; otherwise false.
	 */
	public boolean combinable (Node other){
		// ADD CODE HERE
		if(other==null){
			return false;
		}
		if(other.getLast()+this.getLast()>maxkeys()){
			return false;
		}
		if(other!=getNext()&&other!=getPrev()){
			return false;
		}
		return true;
	}

	/**
       Combines contents of this node and its next sibling (nextsib)
       into a single node
	 */
	public void combine (){
		///////////////////
		// ADD CODE HERE //
		///////////////////
		//combine this node with it's right sibling
		
		
		Node rightSibling = this.getNext();
		Reference parentOfRS = rightSibling.getParent();
		
		if(getLast()==0){
			int newMinKey=rightSibling.getKey(1);
			Reference tempParent = this.getParent();
			int tempPos = tempParent.getIndex();
			while(tempPos==0&&tempParent!=null){
				tempParent = tempParent.getNode().getParent();
				if(tempParent!=null)
					tempPos = tempParent.getIndex();
			}
			if(tempParent!=null)
				tempParent.getNode().keys[tempPos]=newMinKey;
		}
		
		
		this.setNext(rightSibling.getNext());
		if(rightSibling.getNext()!=null){
			rightSibling.getNext().setPrev(this);
		}
		
		int tempIndex = 1;
		while(tempIndex<=rightSibling.getLast()){
			//insert keys to this node
			insertSimple(rightSibling.getKey(tempIndex), null, this.getLast());
			tempIndex++;
		}
		//let parent delete itself
		if(parentOfRS!=null){
			parentOfRS.getNode().delete(parentOfRS.getIndex());
		}
		
	}
	public  void combineLeft(){
		Node leftSibling = this.getPrev();
		Reference parentOfLS = this.getParent();
		
		leftSibling.setNext(this.getNext());
		if(this.getNext()!=null){
			this.getNext().setPrev(leftSibling);
		}
		
		int tempIndex = 1;
		while(tempIndex<=this.getLast()){
			//insert keys to this left sibling
			leftSibling.insertSimple(this.getKey(tempIndex), null, leftSibling.getLast());
			tempIndex++;
		}
		//let parent delete itself
		if(parentOfLS!=null){
			parentOfLS.getNode().delete(parentOfLS.getIndex());
		}
	}
	/**
       Redistributes keys and pointers in this node and its
       next sibling so that they have the same number of keys
       and pointers, or so that this node has one more key and
       one more pointer,.  
       @return int Returns key that must be inserted
       into parent node.
	 */
	public int redistribute (){  
		int key = 0;
		
		///////////////////
		// ADD CODE HERE //
		///////////////////
		if(lastindex==maxkeys()){
			//if the node is full
			int secondNodeStart = (lastindex+1+1)/2+1;
			//get the key which will be inserted into parent
			key = tempKeys[secondNodeStart];
			
			//create second node
			Node secondNode = new LeafNode(degree, key, this.next, this);
			Reference tempParent = getParent();
			if(tempParent!=null){
				secondNode.setParent(new Reference(tempParent.getNode(), tempParent.getIndex()+1, false));
			}
			//add element to second node
			for(int i = secondNodeStart+1;i<=lastindex+1;i++){
				secondNode.insert( tempKeys[i],null);
			}
			//process the old node
			this.lastindex=secondNodeStart-1;
			int tempj = 1;
			while(tempj<=lastindex){
				keys[tempj]=tempKeys[tempj];
				tempj++;
			}
			while(tempj<=degree-1){
				keys[tempj]=0;
				tempj++;
			}
			
		}
		return key;
	}
	public void redistribute (Node sibling){  
		Reference tempThisParent = getParent();
		Node parentNode = tempThisParent.getNode();
		Reference tempSibParent = sibling.getParent();
		// left should have the number of keys
		int half = (this.getLast()+sibling.getLast()+1) / 2;
		
		
		if(tempThisParent.getIndex()>tempSibParent.getIndex()){
			//redistribute with it's left sibling
			while(sibling.getLast()>half){
				insertSimple(sibling.getKey(sibling.getLast()), null,1);
				sibling.deleteSimple(sibling.getLast());
			}
			//replace key of parent
			int newVal = keys[1];
			parentNode.keys[tempThisParent.getIndex()]=newVal;
			
			
		}else{
			//redistribute with it's right sibling
			while(this.getLast()<half){
				insertSimple(sibling.getKey(1), null, getLast());
				sibling.deleteSimple(1);
			}
			//replace key of parent
			int newVal =sibling.keys[1];
			parentNode.keys[tempSibParent.getIndex()]=newVal;
		}
	}
	/**
       Insert val into this node at keys [i].  (Ignores ptr) Called when this
       node is not full.
       @param val the value to insert to current node
       @param ptr not used now, use null when call this method 
       @param i the index where this value should be
	 */
	public void insertSimple (int val, Node ptr, int i){
		///////////////////
		// ADD CODE HERE //
		///////////////////
		//process the lastIndex i
		
		
		if(i==lastindex){
			if(keys[i]<val){
				keys[i+1]=val;
			}else if(keys[i]>val){
				keys[i+1]=keys[i];
				keys[i]=val;
			}
		}else{
			//shift
			int temp = lastindex;
			while(temp>=i){
				keys[temp+1]=keys[temp];
				temp--;
			}
			//assign val
			keys[temp+1]=val;
		}
		
		lastindex++;
	}


	/**
       Deletes keys [i] and ptrs [i] from this node,
       without performing any combination or redistribution afterwards.
       Does so by shifting all keys from index i+1 on
       one position to the left.  
	 */
	public void deleteSimple (int i){
		///////////////////
		// ADD CODE HERE //
		///////////////////	
		//maintain the structure
		if(i==1&&lastindex>1){
			int newVal = keys[i+1];
			Reference tempParent = this.getParent();
			int tempPos = tempParent.getIndex();
			while(tempPos==0&&tempParent!=null){
				tempParent = tempParent.getNode().getParent();
				if(tempParent!=null){
					tempPos = tempParent.getIndex();
				}
			}
			if(tempParent!=null){
				tempParent.getNode().keys[tempPos]=newVal;
			}
		}
		
		//left value of keys
		while(i<lastindex){
			keys[i]=keys[i+1];
			i++;
		}
		keys[i]=0;
		lastindex--;
	} 

	/**
       Uses findKeyIndex, and if val is found, returns the reference with match set to true, otherwise returns
       the reference with match set to false.
       @return a Reference object referring to this node. 
	 */
	public Reference search (int val){
		Reference ref = null;
		///////////////////
		//complete// ADD CODE HERE //
		///////////////////
		int tempIndex = findPtrIndex(val);
		if(keys[tempIndex]==val){
			ref = new Reference(this, tempIndex, true);
		}else{
			ref = new Reference(this, tempIndex, false);
		}
		return ref;
	}

	/**
       Insert val into this, creating split
       and recursive insert into parent if necessary
       Note that ptr is ignored.
       @param val the value to insert
       @param ptr (not used now, use null when calling this method)
	 */
	public void insert (int val, Node ptr){
		///////////////////
		//complete// ADD CODE HERE //
		///////////////////
		int tempIndex = findKeyIndex(val);
		if(lastindex<maxkeys()){
			insertSimple (val,null,tempIndex);
		}else{
			//get real position when tempIndex==lastindex
			if(tempIndex==lastindex){
				if(keys[lastindex]>val){
					tempIndex=lastindex;
				}
				if(keys[lastindex]<val){
					tempIndex=lastindex+1;
				}
			}
			//get tempt keys and ptrs
			tempKeys=new int[degree+1];
			for(int i=1,j=1;j<lastindex+1;i++,j++){
				if(i==tempIndex){
					tempKeys[i]=val;
					i++;
				}
				tempKeys[i]=keys[j];
			}
			if(tempIndex==lastindex+1){
				tempKeys[tempIndex]=val;
			}
			
			//split and get the new key
			int minKeyOfNewNode = redistribute();
			//insert the new key to parent
			Reference parent = getParent();
			if(parent!=null){
				parent.getNode().insert(minKeyOfNewNode, this.next);
			}else{
				new InternalNode(degree, this, minKeyOfNewNode, this.next, null, null);
			}
		}
	}


	/**
       Print to stdout the content of this node
	 */
	void printNode (){
		System.out.print ("[");
		for (int i = 1; i < lastindex; i++) 
			System.out.print (keys[i]+" ");
		System.out.print (keys[lastindex] + "]");
	}
}
