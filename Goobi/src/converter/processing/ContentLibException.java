/*
 * This file is part of the ContentServer  project.
 * Visit the websites for more information. 
 * 		- http://gdz.sub.uni-goettingen.de 
 * 		- http://www.intranda.com 
 * 
 * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
 * intranda software.
 * 
 * Licensed under the Apache License, Version 2.0 ;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package converter.processing;

/************************************************************************************
 * general Exception ImageLib-API
 * 
 * @version 02.01.2009
 * @author Steffen Hankiewicz
 * @author Markus Enders
 ************************************************************************************/
public class ContentLibException extends Exception {

	private static final long serialVersionUID = 4277888305577041996L;

	public ContentLibException(){
		super();
	}
	
	public ContentLibException(String inMessage){
		super(inMessage);
	}

	public ContentLibException(Throwable incause){
		super(incause);
	}

	public ContentLibException(String inMessage, Throwable incause){
		super(inMessage, incause);
	}

}
