package edu.gatech.aristotle.jsretest;
import java.io.*;
import java.util.*;

import edu.gatech.aristotle.jsretest.cfg.*;

import org.mozilla.javascript.ast.*;
import org.mozilla.javascript.*;

public class CFGBuilder {
	
	private ArrayList<ControlFlowGraph<JsSourceNode> > cfgs;
	private LinkedList<AstNode> remainingFunctions;
	
	private JsSourceNodeManager jsNodeManager=new JsSourceNodeManager();
	
	private AstRoot astTree;
	private String sourceDescription;
	
	private int functionsNum=0;
	
	public CFGBuilder(String source,String sourceURI) throws IOException{
		this(new BufferedReader(new FileReader(new File(source))),sourceURI);
	}
	
	public CFGBuilder(Reader source,String sourceURI) throws IOException{
		Parser parser=new Parser();
		sourceDescription=sourceURI;
		astTree=parser.parse(source, sourceURI, 1);
		//System.out.println("Ast tree of "+sourceURI);
		//System.out.println(astTree.debugPrint());
		build();
	}
	
	
	public HashMap<String,ControlFlowGraph<JsSourceNode> > getCFGs(){
		HashMap<String,ControlFlowGraph<JsSourceNode> > cfgSet=new HashMap<String,ControlFlowGraph<JsSourceNode> >();
		for(int i=0;i<cfgs.size();++i){
			cfgSet.put(""+i, cfgs.get(i));
		}
		return cfgSet;
	}
	
	public ControlFlowGraph<JsSourceNode> getEntryFunctionCFG(){
		return cfgs.get(0);
	}
	
	public ControlFlowGraph<JsSourceNode> getFunctionCFG(String functionIdentifier){
		try{
			int functionNo=Integer.parseInt(functionIdentifier);
			return cfgs.get(functionNo);
		}catch(RuntimeException e){
			//Either NumberFormatException or ArrayIndexOutOfBoundException
			//Both mean the specified function identifier is invalid
			return null;
		}
	}
	
	public String getSourceURI(){
		return sourceDescription;
	}
	
	
	private void build(){
		cfgs=new ArrayList<ControlFlowGraph<JsSourceNode> >();
		remainingFunctions=new LinkedList<AstNode>();
		
		addFunctionDefinition(astTree);
		
		while(haveNextFunction()){
			AstNode functionRoot=getNextFunctionDefinition();
			FunctionCFGBuilder functionBuilder=new FunctionCFGBuilder(functionRoot);
			cfgs.add(functionBuilder.getCFG());
		}
	}
	
	private String addFunctionDefinition(AstNode functionDefRoot){
		String functionIdentifier=""+functionsNum++;
		remainingFunctions.addLast(functionDefRoot);
		return functionIdentifier;
	}
	
	private boolean haveNextFunction(){
		return remainingFunctions.size()>0;
	}
	
	private AstNode getNextFunctionDefinition(){
		return remainingFunctions.removeFirst();
	}
	
	//TODO: I realized that function definitions seems to appear any where in the program.
	//Discuss if we need a more consistent way to deal with function definitions.
	private class FunctionCFGBuilder{
		
		private ControlFlowGraph<JsSourceNode> cfg;
		
		private AstNode functionRoot;
		
		public FunctionCFGBuilder(AstNode functionRoot){
			
			this.functionRoot=functionRoot;
			build();
		}
		
		public ControlFlowGraph<JsSourceNode> getCFG(){
			return cfg;
		}
		
		private void build(){
			cfg=new ControlFlowGraph<JsSourceNode>();
			
			//For consistency of the regression selection algorithm, we add this fake node
			//to represent the entry of functions.
			FakeAstNode entryAstNode=new FakeAstNode("entry",0);
			JsSourceNode entrySourceNode=jsNodeManager.getJsSourceNode(entryAstNode);
			cfg.addNode(entrySourceNode);
			cfg.setEntryNode(entrySourceNode);
			
			JsSourceNode sourceNode=jsNodeManager.getJsSourceNode(functionRoot);
			cfg.addNode(sourceNode);
			cfg.addEdge(entrySourceNode, sourceNode);
			
			//Add a fake node to represent the exit of the function
			//We must add the fake node. Otherwise, some control flow information will be lost.
			//e.g. the last statement of the function is the loop condition.
			FakeAstNode exitAstNode=new FakeAstNode("exit",-1);
			JsSourceNode exitSourceNode=jsNodeManager.getJsSourceNode(exitAstNode);
			cfg.addNode(exitSourceNode);
			cfg.addEdge(sourceNode, exitSourceNode);
			cfg.setExitNode(exitSourceNode);
			
			expandControlFlowNode(sourceNode);
		}
		
		private void expandControlFlowNode(JsSourceNode node){
			FindConstructsNodeVisitor findConstructs=new FindConstructsNodeVisitor();
			node.getAstNode().visit(findConstructs);
			ArrayList<AstNode> constructs=findConstructs.getConstructs();
			if(constructs.size()==0){
				//Empty script or function body.
				EmptyStatement emptyStmt=new EmptyStatement();
				emptyStmt.setLineno(node.getLineno());
				constructs.add(emptyStmt);
			}
			if(constructs.size()==1 && (constructs.get(0)==node.getAstNode())){
				
				if(node.getAstNode().isStructuralNode()){
					expandStructuralNode(node);
				}else if(node.getAstNode().isJumpStatement()){
					handleJumpStatement(node);
				}else{
					searchFunctionDefinitions(node.getAstNode());
				}
				return;
			}
			
			ArrayList<ControlFlowEdge<JsSourceNode> > incomingEdges=cfg.getIncomingEdges(node);
			//TODO: Here, actually there should always be exactly one decendent
			//Simplify the code later
			HashMap<String,JsSourceNode> outgoingEdges=cfg.getDecendents(node);
			boolean isEntryNode=(cfg.getEntryNode()==node);
			
			cfg.removeNode(node);
			
			ArrayList<AstNode> structuralNodes=new ArrayList<AstNode>();
			ArrayList<AstNode> jumpStatements=new ArrayList<AstNode>();
			
			JsSourceNode prevNode=null;
			JsSourceNode currNode=null;
			for(int i=0;i<constructs.size();++i){
				AstNode currAstNode=constructs.get(i);
				
				if(currAstNode.isJumpStatement()){
					jumpStatements.add(currAstNode);
				}else if(currAstNode.isStructuralNode()){
					structuralNodes.add(currAstNode);
				}else{
					searchFunctionDefinitions(currAstNode);
				}
				
				currNode=jsNodeManager.getJsSourceNode(currAstNode);
				cfg.addNode(currNode);
				if(prevNode == null){
					//This is the first node in the expanded chain.
					
					//Add the incoming edges.
					for(ControlFlowEdge<JsSourceNode> e:incomingEdges){
						cfg.addEdge(e.getSource(), currNode, e.getLabel());
					}
					//Set this node to be the entry if the parent node is the entry
					if(isEntryNode){
						cfg.setEntryNode(currNode);
					}
					
				}else{
					//Not the first node, connect it to the previous node.
					cfg.addEdge(prevNode, currNode);
				}
				prevNode=currNode;
				
			}
			//Here, currNode and prevNode should point to the same node and this node
			//is the last node in the chain.
			for(String label:outgoingEdges.keySet()){
				cfg.addEdge(currNode, outgoingEdges.get(label), label);
			}
			
			//Then, handle structural nodes and jump statements.
			//I guess it's better to expand all the structural nodes first.
			for(AstNode astNode:structuralNodes){
				expandStructuralNode(jsNodeManager.getJsSourceNode(astNode));
			}
			for(AstNode astNode:jumpStatements){
				handleJumpStatement(jsNodeManager.getJsSourceNode(astNode));
			}
			
			
		}

		private void searchFunctionDefinitions(AstNode astNode) {
			SearchFunctionDefVisitor findFun=new SearchFunctionDefVisitor();
			astNode.visit(findFun);
			ArrayList<FunctionNode> funcs=findFun.getFunctionDefinitions();
			
			for(FunctionNode func:funcs){
				
				String functionId=addFunctionDefinition(func.getBody());
				jsNodeManager.getJsSourceNode(astNode).addFunctionIdentifier(functionId);
			}
			
		}

		
		private void handleJumpStatement(JsSourceNode node) {
			
			AstNode astNode=node.getAstNode();
			AstNode jumpTarget=null;
			switch(astNode.getType()){
			case Token.BREAK:
				BreakStatement breakStmt=(BreakStatement)astNode;
				jumpTarget=breakStmt.getBreakTarget();//.getNext()
				//The break target could be either the innermost structural node or a label.
				//In case of label, we need to use its parent, the LabeledStatement object.
				if(jumpTarget.getType()==Token.LABEL){
					jumpTarget=jumpTarget.getParent();
					if(!(jumpTarget instanceof LabeledStatement)){
						throw new RuntimeException("Unexpected jump target of break statement.");
					}
				}
				jumpTarget=(AstNode)jumpTarget.getNext();
				break;
			case Token.CONTINUE:
				ContinueStatement continueStmt=(ContinueStatement)astNode;
				jumpTarget=continueStmt.getTarget();
				//For DoLoop, the target of enclosed continue statement is the condition, instead of the beginning of the loop.
				if(jumpTarget.getType()==Token.DO){
					DoLoop doNode=(DoLoop)jumpTarget;
					FakeAstNode whileCondition=new FakeAstNode(" while ("+doNode.getCondition().toSource(0)+") ",doNode.getCondition().getLineno());
					jumpTarget=whileCondition;
					
				}
				break;
			case Token.RETURN:
				jumpTarget=cfg.getExitNode().getAstNode();
				break;
			default:
				throw new RuntimeException("Unexpeted jump statement type.");
			}
			
			if(jumpTarget!=null){
				//Here, since we only want to get the first statement, we can change
				//this FindConstructs class to a more specialized one for efficiency later.
				FindConstructsNodeVisitor findConstructs=new FindConstructsNodeVisitor();
				findConstructs.visit(jumpTarget);
				jumpTarget=findConstructs.getConstructs().get(0);
			}
			
			//Remove all existing outgoing edges
			HashMap<String,JsSourceNode> outgoingEdges= cfg.getDecendents(node);
			for(String label:outgoingEdges.keySet()){
				cfg.removeEdge(node, label);
			}
			
			//Add an unconditional control flow to the target node if it exists.
			if(jumpTarget!=null){
				JsSourceNode jumpTargetSourceNode=jsNodeManager.getJsSourceNode(jumpTarget);
				cfg.addNode(jumpTargetSourceNode);
				cfg.addEdge(node, jumpTargetSourceNode);
			}
			
		}

		private void expandStructuralNode(JsSourceNode node) {
			//Keep in mind that expanding a structural node must not
			//change the incoming edges of the node.
			switch(node.getAstNode().getType()){
			case Token.LETEXPR:
			case Token.WITH:
			case Token.CASE:
				expandCappedBlockStructure(node);
				break;
			case Token.FOR:
			case Token.WHILE:
				expandCappedLoopStructure(node);
				break;
			case Token.DO:
				expandDoLoopStructure(node);
				break;
			case Token.IF:
				expandIfStructure(node);
				break;
			case Token.SWITCH:
				expandSwitchStructure(node);
				break;
			case Token.TRY:
				expandTryStructure(node);
				break;
				/* Wrong implementation. Catch clause are handled in try block.
			case Token.CATCH:
				CatchClause catchClause=(CatchClause)node.getAstNode();
				if(catchClause.getCatchCondition()==null){
					expandCappedBlockStructure(node);
				}else{
					expandIfStructure(node);
				}
				*/
			default:
				throw new RuntimeException("Unexpected structural node type.");
			}
			
		}

		private void expandSwitchStructure(JsSourceNode node) {
			
			SwitchStatement switchNode=(SwitchStatement)node.getAstNode();
			
			AstNode switchExpression=switchNode.getExpression();
			searchFunctionDefinitions(switchExpression);
			for(String fid:jsNodeManager.getJsSourceNode(switchExpression).getFunctionIdentifiers()){
				node.addFunctionIdentifier(fid);
			}
			
			List<SwitchCase> cases=switchNode.getCases();
			boolean existsDefaultCase=false;
			
			StringBuilder buffer=new StringBuilder("default-(");
			for(SwitchCase oneCase:cases){
				AstNode expression=oneCase.getExpression();
				if(expression!=null){
					buffer.append(expression.toSource(0));
					buffer.append(":");
				}
			}
			buffer.append(")");
			String defaultLabel=buffer.toString();
			
			JsSourceNode decendent=cfg.getDecendent(node, "");
			if(decendent!=null){
				cfg.removeEdge(node, "");
			}
			
			JsSourceNode prevCaseSourceNode=null;
			for(SwitchCase oneCase:cases){
				AstNode expression=oneCase.getExpression();
				String caseString=null;
				if(expression!=null){
					caseString=expression.toSource(0);
				}else{
					caseString=defaultLabel;
					existsDefaultCase=true;
				}
				JsSourceNode caseSourceNode=jsNodeManager.getJsSourceNode(oneCase);
				cfg.addNode(caseSourceNode);
				cfg.addEdge(node, caseSourceNode, caseString);
				if(prevCaseSourceNode!=null){
					cfg.addEdge(prevCaseSourceNode, caseSourceNode);
				}
				prevCaseSourceNode=caseSourceNode;
				
			}
			
			if(decendent!=null){
				cfg.addEdge(prevCaseSourceNode, decendent, "");
				if(!existsDefaultCase){
					cfg.addEdge(node, decendent, defaultLabel);
				}
			}
			
			for(SwitchCase oneCase:cases){
				expandCappedBlockStructure(jsNodeManager.getJsSourceNode(oneCase));
			}
			
		}

		private void expandTryStructure(JsSourceNode node) {
			// TODO Auto-generated method stub
			
			TryStatement tryStatement=(TryStatement)node.getAstNode();
			
			AstNode tryBody=tryStatement.getTryBlock();
			List<CatchClause> catches=tryStatement.getCatchClauses();
			AstNode finallyBlock=tryStatement.getFinallyBlock();
			
			JsSourceNode finallySourceNode=null;
			if(finallyBlock!=null){
				finallySourceNode=jsNodeManager.getJsSourceNode(finallyBlock);
				cfg.addNode(finallySourceNode);
			}
			
			JsSourceNode decendent=cfg.getDecendent(node, "");
			if(decendent!=null){
				cfg.removeEdge(node, "");
			}
			//First, construct the normal execution flow.
			JsSourceNode tryBlockSourceNode=jsNodeManager.getJsSourceNode(tryBody);
			cfg.addNode(tryBlockSourceNode);
			cfg.addEdge(node, tryBlockSourceNode);
			if(finallySourceNode!=null){
				cfg.addEdge(tryBlockSourceNode, finallySourceNode);
				if(decendent!=null){
					cfg.addEdge(finallySourceNode, decendent);
					cfg.addEdge(finallySourceNode, cfg.getExitNode(),"return_or_throw");
					
				}
			}else{
				if(decendent!=null){
					cfg.addEdge(tryBlockSourceNode, decendent);
				}
			}
			
			//Build the exception flow.
			CatchClause prevCatch=null;
			
			for(CatchClause currCatch:catches){
				
				JsSourceNode catchSourceNode=jsNodeManager.getJsSourceNode(currCatch);
				
				AstNode catchCondition=currCatch.getCatchCondition();
				if(catchCondition!=null){
					searchFunctionDefinitions(catchCondition);
					for(String fid:jsNodeManager.getJsSourceNode(catchCondition).getFunctionIdentifiers()){
						catchSourceNode.addFunctionIdentifier(fid);
					}
				}
				
				cfg.addNode(catchSourceNode);
				JsSourceNode catchBody=jsNodeManager.getJsSourceNode(currCatch.getBody());
				cfg.addNode(catchBody);
				cfg.addEdge(catchSourceNode, catchBody, catchCondition==null?"":"true");
				if(finallySourceNode!=null){
					cfg.addEdge(catchBody, finallySourceNode);
				}else{
					if(decendent!=null){
						cfg.addEdge(catchBody, decendent);
					}
				}
				
				if(prevCatch==null){
					cfg.addEdge(node, catchSourceNode, "exception");
				}else{
					cfg.addEdge(jsNodeManager.getJsSourceNode(prevCatch), jsNodeManager.getJsSourceNode(currCatch), "false");
				}
				
				prevCatch=currCatch;
				
			}
			
			if(prevCatch!=null){
				if(finallySourceNode!=null){
					if(prevCatch.getCatchCondition()!=null){
						cfg.addEdge(jsNodeManager.getJsSourceNode(prevCatch), finallySourceNode,"false");
					}
				}else{
					if(prevCatch.getCatchCondition()!=null){
						cfg.addEdge(jsNodeManager.getJsSourceNode(prevCatch), cfg.getExitNode(),"false");
						
					}
				}
			}else{
				if(finallySourceNode!=null){
					//Only try and finally. Add the exception flow from try to finally
					cfg.addEdge(node, finallySourceNode,"exception");
				}
			}
			
			expandControlFlowNode(tryBlockSourceNode);
			if(finallySourceNode!=null){
				expandControlFlowNode(finallySourceNode);
			}
			for(CatchClause currCatch:catches){
				expandControlFlowNode(jsNodeManager.getJsSourceNode(currCatch.getBody()));
			}
			
		}
 
		private void expandIfStructure(JsSourceNode node) {
			
			AstNode ifCondition=null;//ifNode.getCondition();
			JsSourceNode thenBlock=null;//jsNodeManager.getJsSourceNode(ifNode.getThenPart());
			JsSourceNode elseBlock=null;//jsNodeManager.getJsSourceNode(ifNode.getElsePart());
			
			switch(node.getAstNode().getType()){
			case Token.IF:
				IfStatement ifNode=(IfStatement)node.getAstNode();
				ifCondition=ifNode.getCondition();
				thenBlock=jsNodeManager.getJsSourceNode(ifNode.getThenPart());
				if(ifNode.getElsePart()!=null){
					elseBlock=jsNodeManager.getJsSourceNode(ifNode.getElsePart());
				}
				break;
				/* Wrong implementation. Catch clause are handled separately.
			case Token.CATCH:
				CatchClause catchClause=(CatchClause)node.getAstNode();
				ifCondition=catchClause.getCatchCondition();
				thenBlock=jsNodeManager.getJsSourceNode(catchClause.getBody());
				break;
				*/
			default:
				throw new RuntimeException("Unexpected if node type");
			}
			
			searchFunctionDefinitions(ifCondition);
			ArrayList<String> funcIds=jsNodeManager.getJsSourceNode(ifCondition).getFunctionIdentifiers();
			for(String id:funcIds){
				node.addFunctionIdentifier(id);
			}
			
			JsSourceNode decendent=cfg.getDecendent(node, "");
			if(decendent!=null){
				cfg.removeEdge(node, "");
			}
			
			
			cfg.addNode(thenBlock);
			cfg.addEdge(node, thenBlock, "true");
			if(decendent!=null){
				cfg.addEdge(thenBlock, decendent);
			}
			expandControlFlowNode(thenBlock);
			
			if(elseBlock!=null){
				
				
				cfg.addNode(elseBlock);
				cfg.addEdge(node, elseBlock, "false");
				if(decendent!=null){
					cfg.addEdge(elseBlock, decendent);
				}
				expandControlFlowNode(elseBlock);
			}else{
				if(decendent!=null){
					cfg.addEdge(node, decendent,"false");
				}
				
			}
			
		}

		private void expandDoLoopStructure(JsSourceNode node) {
			
			DoLoop doNode=(DoLoop)node.getAstNode();
			
			JsSourceNode decendent=cfg.getDecendent(node, "");
			if(decendent!=null){
				cfg.removeEdge(node, "");
			}
			
			JsSourceNode loopBody=jsNodeManager.getJsSourceNode(doNode.getBody());
			FakeAstNode whileCondition=new FakeAstNode(" while ("+doNode.getCondition().toSource(0)+") ",doNode.getCondition().getLineno());
			JsSourceNode conditionSourceNode=jsNodeManager.getJsSourceNode(whileCondition);
			
			
			AstNode condition=doNode.getCondition();
			searchFunctionDefinitions(condition);
			ArrayList<String> funcIds=jsNodeManager.getJsSourceNode(condition).getFunctionIdentifiers();
			for(String id:funcIds){
				conditionSourceNode.addFunctionIdentifier(id);
			}
			
			cfg.addNode(loopBody);
			cfg.addEdge(node, loopBody);
			
			cfg.addNode(conditionSourceNode);
			cfg.addEdge(loopBody, conditionSourceNode);
			cfg.addEdge(conditionSourceNode, loopBody,"true");
			if(decendent!=null){
				cfg.addEdge(conditionSourceNode, decendent, "false");
			}
			
			expandControlFlowNode(loopBody);
		}

		private void expandCappedLoopStructure(JsSourceNode node) {
			
			AstNode loopBody=null;
			switch(node.getAstNode().getType()){
			case Token.FOR:
				if(node.getAstNode() instanceof ForInLoop){
					ForInLoop astNode=(ForInLoop)node.getAstNode();
					loopBody=astNode.getBody();
					
					AstNode iterator=astNode.getIterator();
					searchFunctionDefinitions(iterator);
					for(String id:jsNodeManager.getJsSourceNode(iterator).getFunctionIdentifiers()){
						node.addFunctionIdentifier(id);
					}
					
					AstNode iteratedObj=astNode.getIteratedObject();
					searchFunctionDefinitions(iteratedObj);
					for(String id:jsNodeManager.getJsSourceNode(iteratedObj).getFunctionIdentifiers()){
						node.addFunctionIdentifier(id);
					}
				}else if(node.getAstNode() instanceof ForLoop){
					ForLoop astNode=(ForLoop)node.getAstNode();
					loopBody=astNode.getBody();
					
					AstNode initializer=astNode.getInitializer();
					searchFunctionDefinitions(initializer);
					for(String id:jsNodeManager.getJsSourceNode(initializer).getFunctionIdentifiers()){
						node.addFunctionIdentifier(id);
					}
					
					AstNode condition=astNode.getCondition();
					searchFunctionDefinitions(condition);
					for(String id:jsNodeManager.getJsSourceNode(condition).getFunctionIdentifiers()){
						node.addFunctionIdentifier(id);
					}
					
					AstNode incrementor=astNode.getIncrement();
					searchFunctionDefinitions(incrementor);
					for(String id:jsNodeManager.getJsSourceNode(incrementor).getFunctionIdentifiers()){
						node.addFunctionIdentifier(id);
					}
				}else{
					throw new RuntimeException("Unexpected for loop type.");
				}
				break;
			case Token.WHILE:
				WhileLoop astNode=(WhileLoop)node.getAstNode();
				loopBody=astNode.getBody();
				
				AstNode condition=astNode.getCondition();
				searchFunctionDefinitions(condition);
				for(String id:jsNodeManager.getJsSourceNode(condition).getFunctionIdentifiers()){
					node.addFunctionIdentifier(id);
				}
				break;
			default:
				throw new RuntimeException("Unexpected capped loop node type.");
			}
			
			JsSourceNode decendent=cfg.getDecendent(node, "");
			if(decendent!=null){
				cfg.removeEdge(node, "");
			}
			
			JsSourceNode loopBodySourceNode=jsNodeManager.getJsSourceNode(loopBody);
			cfg.addNode(loopBodySourceNode);
			cfg.addEdge(node, loopBodySourceNode,"true");
			cfg.addEdge(loopBodySourceNode, node);
			if(decendent!=null){
				cfg.addEdge(node, decendent,"false");
			}
			
			expandControlFlowNode(loopBodySourceNode);
		}

		private void expandCappedBlockStructure(JsSourceNode node) {
			
			AstNode cappingBlock=null;
			AstNode cappedBody=null;
			
			switch(node.getAstNode().getType()){
			case Token.LETEXPR:
				LetNode letNode=(LetNode)node.getAstNode();
				cappingBlock=letNode.getVariables();
				cappedBody=letNode.getBody();
				break;
			case Token.WITH:
				WithStatement withNode=(WithStatement)node.getAstNode();
				cappingBlock=withNode.getExpression();
				cappedBody=withNode.getStatement();
				break;
				/* Wrong implementation. Catch clause are handled separately.
			case Token.CATCH:
				CatchClause catchClause=(CatchClause)node.getAstNode();
				cappingBlock=catchClause.getCatchCondition();
				cappedBody=catchClause.getBody();
				break;
				*/
			case Token.CASE:
				SwitchCase caseNode=(SwitchCase)node.getAstNode();
				cappingBlock=caseNode.getExpression();
				List<AstNode> statements=caseNode.getStatements();
				
				if(statements!=null && !statements.isEmpty()){
					//TODO: Verify if this is OK.
					Block block=new Block();
					for(AstNode statement:statements){
						block.addStatement(statement);
					}
					cappedBody=block;
				}
				break;
			default:
				throw new RuntimeException("Unexpected capped block type.");
				
			}
			if(cappingBlock!=null){
				searchFunctionDefinitions(cappingBlock);
				for(String id:jsNodeManager.getJsSourceNode(cappingBlock).getFunctionIdentifiers()){
					node.addFunctionIdentifier(id);
				}
			}
			
			if(cappedBody==null){
				//The body is empty. Do nothing
				return;
			}
			
			//Else, insert the capped body to the flow
			
			JsSourceNode decendent=cfg.getDecendent(node, "");
			if(decendent!=null){
				cfg.removeEdge(node, "");
			}
			
			JsSourceNode cappedBodySourceNode=jsNodeManager.getJsSourceNode(cappedBody);
			cfg.addNode(cappedBodySourceNode);
			cfg.addEdge(node, cappedBodySourceNode,"");
			
			if(decendent!=null){
				cfg.addEdge(cappedBodySourceNode, decendent,"");
			}
			
			expandControlFlowNode(cappedBodySourceNode);
			
		}
		
		
	}
	
	private class FindConstructsNodeVisitor implements NodeVisitor{

		private ArrayList<AstNode> topLevelConstructs=new ArrayList<AstNode>();
		
		@SuppressWarnings("unused")
		public void clear(){
			topLevelConstructs=new ArrayList<AstNode>();
		}
		
		public ArrayList<AstNode> getConstructs(){
			return topLevelConstructs;
		}
		
		@Override
		public boolean visit(AstNode node) {
			if(node.isSingleStatement() || node.isStructuralNode()){
				topLevelConstructs.add(node);
				return false;
			}
			if(!node.hasChildren()){
				//We already know of these two types and the reason why they will definitely be here.
				if(!(node instanceof LabeledStatement || node instanceof Label)){
					System.err.println("Warning: Node information not collected!");
					System.err.println(node.debugPrint());
				}
			}
			return true;
		}
		
	}
	
	private class SearchFunctionDefVisitor implements NodeVisitor{

		ArrayList<FunctionNode> functionDefs=new ArrayList<FunctionNode>();
		
		@SuppressWarnings("unused")
		public void clear(){
			functionDefs=new ArrayList<FunctionNode>();
		}
		
		public ArrayList<FunctionNode> getFunctionDefinitions(){
			return functionDefs;
		}
		
		@SuppressWarnings("unused")
		public boolean hasFunctionDefinition(){
			return functionDefs.size()>0;
		}
		
		@Override
		public boolean visit(AstNode node) {
			if(node.getType()==Token.FUNCTION){
				functionDefs.add((FunctionNode)node);
				return false;
			}else{
				return true;
			}
		}
		
	}
	
	private class JsSourceNodeManager{
		
		private HashMap<AstNode,JsSourceNode> nodesMap=new HashMap<AstNode,JsSourceNode>();
		
		public JsSourceNode getJsSourceNode(AstNode astNode){
			if(astNode == null){
				throw new IllegalArgumentException("AstNode cannot be null.");
			}
			JsSourceNode jsNode=nodesMap.get(astNode);
			if(jsNode == null){
				jsNode=new JsSourceNode(astNode);
				nodesMap.put(astNode, jsNode);
			}
			return jsNode;
		}
	}

}



