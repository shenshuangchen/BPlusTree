package edu.brandeis.cs127.pa3;

/**
    Internal Nodes of B+-Trees.
    @author cs127b
 */
public class InternalNode extends Node{
	private int[] tempKeys=null;
	private Node[] tempPtrs = null;
	/**
       Construct an InternalNode object and initialize it with the parameters.
       @param d degree
       @param p0 the pointer at the left of the key
       @param k1 the key value
       @param p1 the pointer at the right of the key
       @param n the next node
       @param p the previous node
	 */
	public InternalNode (int d, Node p0, int k1, Node p1, Node n, Node p){

		super (d, n, p);
		ptrs [0] = p0;
		keys [1] = k1;
		ptrs [1] = p1;
		lastindex = 1;

		if (p0 != null) p0.setParent (new Reference (this, 0, false));
		if (p1 != null) p1.setParent (new Reference (this, 1, false));
	}

	/**
       The minimal number of keys this node should have.
       @return the minimal number of keys a leaf node should have.
	 */
	public int minkeys () {
		int min = 0;
		///////////////////
		//complete// ADD CODE HERE //
		///////////////////
		//minimal number of key
		min = (degree+1)/2-1;
		//if the node is a root, the number is 1
		if(parentref==null){
			min = 1;
		}
		return min;
	}

	/**
       Check if this node can be combined with other into a new node without splitting.
       Return TRUE if this node and other can be combined. 
	 */
	public boolean combinable (Node other) {

		boolean combinable = true;
		///////////////////
		// ADD CODE HERE //
		///////////////////
		if(other==null){
			combinable = false;
		}else if(other.getLast()+this.getLast()>maxkeys()-1){
			combinable = false;
		}else if(other!=getNext()&&other!=getPrev()){
			combinable = false;
		}
		return combinable;
	}


	/**
       Combines contents of this node and its next sibling (next)
       into a single node,
	 */
	public void combine () {
		///////////////////
		// ADD CODE HERE //
		///////////////////
		//combine this node with it's right sibling
		Node rightSibling = this.getNext();
		Reference parentOfRS = rightSibling.getParent();
		
		this.setNext(rightSibling.getNext());
		if(rightSibling.getNext()!=null){
			rightSibling.getNext().setPrev(this);
		}
		//get parent's last key and insert into the itself
		insertSimple(parentOfRS.getNode().getKey(parentOfRS.getIndex()), rightSibling.getPtr(0), this.getLast());
		
		int tempIndex = 1;
		while(tempIndex<=rightSibling.getLast()){
			//insert keys to this node
			insertSimple(rightSibling.getKey(tempIndex), rightSibling.getPtr(tempIndex), this.getLast());
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
		//get parent's last key and insert into the left sibling
		leftSibling.insertSimple(parentOfLS.getNode().getKey(parentOfLS.getIndex()), this.getPtr(0), leftSibling.getLast());
				
		int tempIndex = 1;
		while(tempIndex<=this.getLast()){
			//insert keys to this left sibling
			leftSibling.insertSimple(this.getKey(tempIndex), getPtr(tempIndex), leftSibling.getLast());
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
       one more pointer.  Returns the key that must be inserted
       into parent node.
       @return the value to be inserted to the parent node
	 */
	public int redistribute () {
		int key = 0;
		///////////////////
		// ADD CODE HERE // 
		///////////////////
		if(lastindex==maxkeys()){
			
			//if the node is full
			int secondNodeStart = (lastindex+1+2)/2+1;
			//get the key which will be inserted into parent
			key = tempKeys[secondNodeStart-1];
			//create second node
			Node secondNode = new InternalNode(degree,  tempPtrs[secondNodeStart-1],  tempKeys[secondNodeStart], tempPtrs[secondNodeStart], this.next, this);
			Reference tempParent = getParent();
			if(tempParent!=null){
				secondNode.setParent(new Reference(tempParent.getNode(), tempParent.getIndex()+1, false));
			}	//add element to second node
			for(int i = secondNodeStart+1;i<=lastindex+1;i++){
				secondNode.insert( tempKeys[i], tempPtrs[i]);
			}
			//process the old node
			this.lastindex=secondNodeStart-2;
			int tempj = 1;
			while(tempj<=lastindex){
				keys[tempj]=tempKeys[tempj];
				ptrs[tempj]=tempPtrs[tempj];
				ptrs[tempj].setParent(new Reference(this, tempj, false));
				tempj++;
			}
			while(tempj<=degree-1){
				keys[tempj]=0;
				ptrs[tempj]=null;
				tempj++;
			}
		}else if(lastindex<minkeys()){
			
		}
		return key;

	}
	public void redistribute (Node sibling){  
		// left should have the number of keys
		int half = (this.getLast()+sibling.getLast()+1) / 2;
		tempKeys = new int[this.getLast()+sibling.getLast()+5];
		tempPtrs = new Node[this.getLast()+sibling.getLast()+5];
		
		
		
		Reference tempThisParent = getParent();
		Node parentNode = tempThisParent.getNode();
		Reference tempSibParent = sibling.getParent();
		if(tempThisParent.getIndex()>tempSibParent.getIndex()){
			//redistribute with it's left sibling
			int tempParentKey = parentNode.keys[tempThisParent.getIndex()];
			
			int i=1,j=1;
			while(j<=sibling.getLast()){
				tempKeys[i]=sibling.keys[j];
				tempPtrs[i++]=sibling.ptrs[j];
				j++;
			}
			
			tempKeys[i]=tempParentKey;
			tempPtrs[i++]=ptrs[0];
			j=1;
			while(j<=this.getLast()){
				tempKeys[i]=this.keys[j];
				tempPtrs[i++]=this.ptrs[j];
				j++;
			}
			
			insertLeftDef(tempKeys[sibling.getLast()+1], tempPtrs[sibling.getLast()], 1);
			sibling.deleteSimple(sibling.getLast());
			
			//redistribute with it's left sibling
			while(sibling.getLast()>half){
				insertLeftDef(tempKeys[sibling.getLast()+1], tempPtrs[sibling.getLast()+1], 1);
				sibling.deleteSimple(sibling.getLast());
			}
			
			//replace key of parent
			int newVal =tempKeys[half+1];
			parentNode.keys[tempThisParent.getIndex()]=newVal;
			
			
		}else{
			//redistribute with it's right sibling
			int tempParentKey = parentNode.keys[tempSibParent.getIndex()];
			
			int i=1,j=1;
			
			while(j<=this.getLast()){
				tempKeys[i]=this.keys[j];
				tempPtrs[i++]=this.ptrs[j];
				j++;
			}

			tempKeys[i]=tempParentKey;
			tempPtrs[i]=sibling.ptrs[0];
			i++;
			j=1;
			while(j<=sibling.getLast()){
				tempKeys[i]=sibling.keys[j];
				tempPtrs[i++]=sibling.ptrs[j];
				j++;
			}
			insertSimple(tempKeys[getLast()+1], tempPtrs[getLast()+1], getLast());
			sibling.shiftLeftDef(1);
			while(this.getLast()<half){
				insertSimple(tempKeys[getLast()+1], tempPtrs[getLast()+1], getLast());
				sibling.shiftLeftDef(1);
			}
			//replace key of parent
			int newVal =tempKeys[half+1];
			parentNode.keys[tempSibParent.getIndex()]=newVal;
			
		}
	}
	

	
	/**
       Inserts (val, ptr) pair into this node
       at keys [i] and ptrs [i].  Called when this
       node is not full.  Differs from {@link LeafNode} routine in
       that updates parent references of all ptrs from index i+1 on.
       @param val the value to insert
       @param ptr the pointer to insert 
       @param i the position to insert the value and pointer
	 */
	public void insertSimple (int val, Node ptr, int i) {
		////////////////////
		// ADD CODE HERE  //
		////////////////////
		//process the lastIndex i
		if(i==lastindex){
			if(keys[i]<val){
				keys[i+1]=val;
				ptrs[i+1]=ptr;
				ptr.setParent(new Reference(this, i+1, false));
			}else if(keys[i]>val){
				keys[i+1]=keys[i];
				keys[i]=val;
				ptrs[i+1]=ptrs[i];
				ptrs[i+1].getParent().increaseIndex();
				ptrs[i]=ptr;
				ptr.setParent(new Reference(this, i, false));
			}
		}else{
			//shift
			int temp = lastindex;
			while(temp>=i){
				keys[temp+1]=keys[temp];
				ptrs[temp+1]=ptrs[temp];
				ptrs[temp+1].getParent().increaseIndex();
				temp--;
			}
			//assign val
			keys[temp+1]=val;
			ptrs[temp+1]=ptr;
			ptr.setParent(new Reference(this, temp+1, false));
		}
		lastindex++;
	}

	/**
       Deletes keys [i] and ptrs [i] from this node,
       without performing any combination or redistribution afterwards.
       Does so by shifting all keys and pointers from index i+1 on
       one position to the left.  Differs from {@link LeafNode} routine in
       that updates parent references of all ptrs from index i+1 on.
       @param i the index of the key to delete
	 */
	public void deleteSimple (int i) {
		///////////////////
		// ADD CODE HERE //
		///////////////////
	
		
		while(i<lastindex){
			keys[i]=keys[i+1];
			ptrs[i]=ptrs[i+1];
			ptrs[i].getParent().decreaseIndex();
			i++;
		}

		keys[lastindex]=0;
		ptrs[lastindex]=null;
		lastindex--;
	}


	/**
       Uses findPtrInex and calles itself recursively until find the value or pind the position 
       where the value should be.
       @return the referenene pointing to a leaf node.
	 */
	public Reference search (int val) {
		Reference ref = null;
		///////////////////
		//complete// ADD CODE HERE //
		///////////////////
		int ptrIndexTemp = findPtrIndex(val);
		Node temp = ptrs[ptrIndexTemp];
		ref = temp.search(val);
		
		return ref;
	}

	/**
       Insert (val, ptr) into this node. Uses insertSimple, redistribute etc.
       Insert into parent recursively if necessary
       @param val the value to insert
       @param ptr the pointer to insert 
	 */
	public void insert (int val, Node ptr) {
		///////////////////
		// ADD CODE HERE //
		///////////////////
		int tempIndex = findKeyIndex(val);
		if(lastindex<maxkeys()){
			insertSimple (val,ptr,tempIndex);
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
			tempPtrs = new Node[degree+1];
			for(int i=1,j=1;j<lastindex+1;i++,j++){
				if(i==tempIndex){
					tempKeys[i]=val;
					tempPtrs[i]=ptr;
					i++;
				}
				tempKeys[i]=keys[j];
				tempPtrs[i]=ptrs[j];
			}
			if(tempIndex==lastindex+1){
				tempKeys[tempIndex]=val;
				tempPtrs[tempIndex]=ptr;
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

	public void outputForGraphviz() {

		// The name of a node will be its first key value
		// String name = "I" + String.valueOf(keys[1]);
		// name = BTree.nextNodeName();

		// Now, prepare the label string
		String label = "";
		for (int j = 0; j <= lastindex; j++) {
			if (j > 0) label += "|";
			label += "<p" + ptrs[j].myname + ">";
			if (j != lastindex) label += "|" + String.valueOf(keys[j+1]);
			// Write out any link now
			BTree.writeOut(myname + ":p" + ptrs[j].myname + " -> " + ptrs[j].myname + "\n");
			// Tell your child to output itself
			ptrs[j].outputForGraphviz();
		}
		// Write out this node
		BTree.writeOut(myname + " [shape=record, label=\"" + label + "\"];\n");
	}

	/**
       Print out the content of this node
	 */
	void printNode () {

		int j;
		System.out.print("[");
		for (j = 0; j <= lastindex; j++) {

			if (j == 0)
				System.out.print (" * ");
			else
				System.out.print(keys[j] + " * ");

			if (j == lastindex)
				System.out.print ("]");
		}
	}
}


