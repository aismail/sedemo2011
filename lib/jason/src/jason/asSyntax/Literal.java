// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 This class represents an abstract literal (an Atom, Structure, Predicate, etc), it is mainly
 the interface of a literal. 
 
 To create a new Literal, one of the following concrete classes may be used:
 <ul>
 <li> Atom -- the most simple literal, is composed by only a functor (no term, no annots);
 <li> Structure -- has functor and terms;
 <li> Pred -- has functor, terms, and annotations;
 <li> LiteralImpl -- Pred + negation. 
 </ul>
 The latter class supports all the operations of 
 the Literal interface.
 
 <p>There are useful static methods in class {@link ASSyntax} to create Literals.
 
 @navassoc - type - PredicateIndicator
 @opt nodefillcolor lightgoldenrodyellow

 @author jomi
 
 @see ASSyntax
 @see Atom
 @see Structure
 @see Pred
 @see LiteralImpl
 
 */
public abstract class Literal extends DefaultTerm implements LogicalFormula {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(Literal.class.getName());
    
    public static final boolean LPos   = true;
    public static final boolean LNeg   = false;
    public static final Literal LTrue  = new TrueLiteral();
    public static final Literal LFalse = new FalseLiteral();

    protected PredicateIndicator predicateIndicatorCache = null; // to not compute it all the time (it is used many many times)
    
    /** creates a new literal by parsing a string -- ASSyntax.parseLiteral or createLiteral are preferred. */
    public static Literal parseLiteral(String sLiteral) {
        try {
            as2j parser = new as2j(new StringReader(sLiteral));
            return parser.literal();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error parsing literal " + sLiteral,e);
            return null;
        }
    }
    
    public Literal copy() {
        return (Literal)clone(); // should call the clone, that is overridden in subclasses
    }

    /** returns the functor of this literal */
    public abstract String getFunctor();
    
    @Override
    public boolean isLiteral() {
        return true;
    }
    
    /** returns functor symbol "/" arity */
    public PredicateIndicator getPredicateIndicator() {
        if (predicateIndicatorCache == null) {
            predicateIndicatorCache = new PredicateIndicator(getFunctor(),getArity());
        }
        return predicateIndicatorCache;
    }

    /* default implementation of some methods */

    /** returns the number of terms of this literal */
    public int getArity()         { return 0;  }
    /** returns true if this literal has some term */
    public boolean hasTerm()      { return false; } 
    /** returns all terms of this literal */
    public List<Term> getTerms()  { return Structure.emptyTermList;   }
    /** returns all terms of this literal as an array */
    public Term[] getTermsArray() { return getTerms().toArray(Structure.emptyTermArray);  }
    
    private static final List<VarTerm> emptyListVar = new ArrayList<VarTerm>();
    /** returns all singleton vars (that appears once) in this literal */
    public List<VarTerm> getSingletonVars() { return emptyListVar; }

    /** replaces all terms by unnamed variables (_). */
    public void makeTermsAnnon()  {}
    /** replaces all variables by unnamed variables (_). */
    public Literal makeVarsAnnon()   { return this; }

    /**
     * replaces all variables of the term for unnamed variables (_).
     * 
     * @param un is the unifier that contains the map of replacements
     */
    public Literal makeVarsAnnon(Unifier un) { return this; }

    /** returns all annotations of the literal */
    public ListTerm getAnnots()     { return null; }
    /** returns true if there is some annotation <i>t</i> in the literal */
    public boolean hasAnnot(Term t) { return false; }

    /** returns true if the pred has at least one annot */
    public boolean hasAnnot()       { return false; }
    
    /** returns true if all this predicate annots are in p's annots */
    public boolean hasSubsetAnnot(Literal p)            { return true; }
    
    /**
     * Returns true if all this predicate's annots are in p's annots using the
     * unifier u. 
     *
     * if p annots has a Tail, p annots's Tail will receive this predicate's annots,
     * e.g.: 
     *   this[a,b,c] = p[x,y,b|T]
     * unifies and T is [a,c] (this will be a subset if p has a and c in its annots).
     *
     * if this annots has a tail, the Tail will receive all necessary term
     * to be a subset, e.g.:
     *   this[b|T] = p[x,y,b]
     * unifies and T is [x,y] (this will be a subset if T is [x,y].
     */
    public boolean hasSubsetAnnot(Literal p, Unifier u) { return true; }

    /** removes all annotations */
    public void    clearAnnots()    { }
    
    /**
     * returns all annots with the specified functor e.g.: from annots
     * [t(a), t(b), source(tom)]
     * and functor "t",
     * it returns [t(a),t(b)]
     * 
     * in case that there is no such an annot, it returns an empty list. 
     */
    public ListTerm getAnnots(String functor) { return new ListTermImpl(); }
    /**
     * returns the sources of this literal as a new list. e.g.: from annots
     * [source(a), source(b)], it returns [a,b]
     */
    public ListTerm getSources()    { return new ListTermImpl(); }
    /** returns true if this literal has some source annotation */
    public boolean hasSource()      { return false; }
    /** returns true if this literal has a "source(<i>agName</i>)" */
    public boolean hasSource(Term agName) { return false; }

    /** returns this if this literal can be added in the belief base (Atoms, for instance, can not be) */
    public boolean canBeAddedInBB() { return false; }
    
    /** returns whether this literal is negated or not, use Literal.LNeg and Literal.LPos to compare the returned value */
    public boolean negated()        { return false; }

    public boolean equalsAsStructure(Object p) { return false;  }
    
	/* Not implemented methods */
	
    // structure
    public void addTerm(Term t)              { logger.log(Level.SEVERE, "addTerm is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }
    public void delTerm(int index)           { logger.log(Level.SEVERE, "delTerm is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }
    /** adds some terms and return this */
    public Literal addTerms(Term ... ts )    { logger.log(Level.SEVERE, "addTerms is not implemented in the class "+this.getClass().getSimpleName(), new Exception()); return null; }
    /** adds some terms and return this */
    public Literal addTerms(List<Term> l)    { logger.log(Level.SEVERE, "addTerms is not implemented in the class "+this.getClass().getSimpleName(), new Exception()); return null; }
    /** returns the i-th term (first term is 0) */
    public Term getTerm(int i)               { logger.log(Level.SEVERE, "getTerm(i) is not implemented in the class "+this.getClass().getSimpleName(), new Exception()); return null; }
    /** set all terms of the literal and return this */
    public Literal setTerms(List<Term> l)    { logger.log(Level.SEVERE, "setTerms is not implemented in the class "+this.getClass().getSimpleName(), new Exception()); return null; }
    public void setTerm(int i, Term t)       { logger.log(Level.SEVERE, "setTerm is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }
    
    // pred
    public Literal setAnnots(ListTerm l)         { logger.log(Level.SEVERE, "setAnnots is not implemented in the class "+this.getClass().getSimpleName(), new Exception()); return null; }
    public boolean addAnnot(Term t)          { logger.log(Level.SEVERE, "addAnnot("+t+") is not implemented in the class "+this.getClass().getSimpleName()+" of object "+this, new Exception()); return false; }
    
    /** adds some annots and return this */
    public Literal addAnnots(Term ... terms) { logger.log(Level.SEVERE, "addAnnots is not implemented in the class "+this.getClass().getSimpleName(), new Exception()); return null; }

    /** adds some annots and return this */
    public Literal addAnnots(List<Term> l)   { logger.log(Level.SEVERE, "addAnnots is not implemented in the class "+this.getClass().getSimpleName(), new Exception()); return null; }
    public boolean delAnnot(Term t)          { logger.log(Level.SEVERE, "delAnnot is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  return false; }

    /**
     * removes all annots in this pred that are in the list <i>l</i>.
     * @return true if some annot was removed.
     */
    public boolean delAnnots(List<Term> l)   { logger.log(Level.SEVERE, "delAnnots is not implemented in the class "+this.getClass().getSimpleName(), new Exception()); return false; }

    /**
     * "import" annots from another predicate <i>p</i>. p will be changed
     * to contain only the annots actually imported (for Event), 
     * for example:
     *     p    = b[a,b] 
     *     this = b[b,c] 
     *     after import, p = b[a] 
     * It is used to generate event <+b[a]>.
     * 
     * @return true if some annot was imported.
     */
    public boolean importAnnots(Literal p)   { logger.log(Level.SEVERE, "importAnnots is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  return false; }

    /** adds the annotation source(<i>agName</i>) */
    public void addSource(Term agName)       { logger.log(Level.SEVERE, "addSource is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }
    /** deletes one source(<i>agName</i>) annotation, return true if deleted */
    public boolean delSource(Term agName)    { logger.log(Level.SEVERE, "delSource is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  return false; }
    /** deletes all source annotations */
    public void delSources()                 { logger.log(Level.SEVERE, "delSources is not implemented in the class "+this.getClass().getSimpleName(), new Exception());  }

    // literal    
    /** changes the negation of the literal and return this */
    public Literal setNegated(boolean b)     { logger.log(Level.SEVERE, "setNegated is not implemented in the class "+this.getClass().getSimpleName(), new Exception()); return null; }
    
    
    /** 
     * logicalConsequence checks whether one particular predicate
     * is a logical consequence of the belief base.
     * 
     * Returns an iterator for all unifiers that are logCons.
     */
    public Iterator<Unifier> logicalConsequence(final Agent ag, final Unifier un) {
        final Iterator<Literal> il   = ag.getBB().getCandidateBeliefs(this, un);
        if (il == null) // no relevant bels
            return LogExpr.EMPTY_UNIF_LIST.iterator();
        
        final AgArch            arch     = (ag == null ? null : ag.getTS().getUserAgArch());
        final int               nbAnnots = (hasAnnot() && getAnnots().getTail() == null ? getAnnots().size() : 0); // if annots contains a tail (as in p[A|R]), do not backtrack on annots
        
        return new Iterator<Unifier>() {
            Unifier           current = null;
            Iterator<Unifier> ruleIt = null; // current rule solutions iterator
            Literal           cloneAnnon = null; // a copy of the literal with makeVarsAnnon
            Rule              rule; // current rule
            boolean           needsUpdate = true;
            
            Iterator<List<Term>>  annotsOptions = null;
            Literal               belInBB = null;
            
            public boolean hasNext() {
                if (needsUpdate)
                    get();
                return current != null;
            }

            public Unifier next() {
                if (needsUpdate)
                    get();
                Unifier a = current;
                if (current != null)
                    needsUpdate = true;
                return a;
            }

            private void get() {
                needsUpdate = false;
                current     = null;
                if (arch != null && !arch.isRunning()) return;
                
                // try annots iterator
                while (annotsOptions != null && annotsOptions.hasNext()) {
                    Literal belToTry = belInBB.copy().setAnnots(null).addAnnots( annotsOptions.next() );
                    
                    Unifier u = un.clone();
                    if (u.unifiesNoUndo(Literal.this, belToTry)) {
                        current = u;
                        return;
                    }                    
                }
                
                // try rule iterator
                while (ruleIt != null && ruleIt.hasNext()) {
                    // unifies the rule head with the result of rule evaluation
                    Unifier ruleUn = ruleIt.next(); // evaluation result
                    Literal rhead  = rule.headClone();
                    rhead.apply(ruleUn);
                    useDerefVars(rhead, ruleUn); // replace vars by the bottom in the var clusters (e.g. X=_2; Y=_2, a(X,Y) ===> A(_2,_2))
                    rhead.makeVarsAnnon(); // to remove vars in head with original names
                    
                    Unifier unC = un.clone();
                    if (unC.unifiesNoUndo(Literal.this, rhead)) {
                        current = unC;
                        return;
                    }
                }
                
                // try literal iterator
                while (il.hasNext()) {
                    belInBB = il.next(); // b is the relevant entry in BB
                    if (belInBB.isRule()) {
                        rule = (Rule)belInBB;
                        
                        // create a copy of this literal, ground it and 
                        // make its vars anonymous, 
                        // it is used to define what will be the unifier used
                        // inside the rule.
                        if (cloneAnnon == null) {
                            cloneAnnon = Literal.this.copy();
                            cloneAnnon.apply(un);
                            cloneAnnon.makeVarsAnnon();
                        }
                        Unifier ruleUn = new Unifier();
                        if (ruleUn.unifiesNoUndo(cloneAnnon, rule)) { // the rule head unifies with the literal
                            ruleIt = rule.getBody().logicalConsequence(ag,ruleUn);
                            get();
                            if (current != null) // if it get a value
                                return;
                        }
                    } else { // not rule
                        if (nbAnnots > 0) { // try annots backtracking
                            if (belInBB.hasAnnot()) {
                                int nbAnnotsB = belInBB.getAnnots().size();
                                if (nbAnnotsB >= nbAnnots) {
                                    annotsOptions = belInBB.getAnnots().subSets( nbAnnots );
                                    get();
                                    if (current != null) // if it get a value
                                        return;
                                }
                            }
                        } else {
                            Unifier u = un.clone();
                            if (u.unifiesNoUndo(Literal.this, belInBB)) {
                                current = u;
                                return;
                            }
                        }
                    }
                }
            }
            

            public void remove() {}
        };
    }   
    
    
    private void useDerefVars(Term p, Unifier un) {
        if (p instanceof Literal) {
            Literal l = (Literal)p;
            for (int i=0; i<l.getArity(); i++) {
                Term t = l.getTerm(i);
                if (t.isVar()) {
                    l.setTerm(i, un.deref( (VarTerm)t));
                } else {
                    useDerefVars(t, un);                    
                }
            }
        }
    }
    
	/** returns this literal as a list with three elements: [functor, list of terms, list of annots] */
	public ListTerm getAsListOfTerms() {
		ListTerm l = new ListTermImpl();
		l.add(new LiteralImpl(!negated(), getFunctor()));
		ListTerm lt = new ListTermImpl();
		lt.addAll(getTerms());
		l.add(lt);
		if (hasAnnot()) {
		    l.add(getAnnots().cloneLT());
		} else {
		    l.add(new ListTermImpl());
		}
		return l;
	}

	/** creates a literal from a list with three elements: [functor, list of terms, list of annots] */
	public static Literal newFromListOfTerms(ListTerm lt) throws JasonException {
		try {
			Iterator<Term> i = lt.iterator();
			
			Term tfunctor = i.next();

			boolean pos = Literal.LPos;
			if (tfunctor.isLiteral() && ((Literal)tfunctor).negated()) {
				pos = Literal.LNeg;
			}

			Literal l = new LiteralImpl(pos,((Atom)tfunctor).getFunctor());

			if (i.hasNext()) {
				l.setTerms(((ListTerm)i.next()).cloneLT());
			}
			if (i.hasNext()) {
				l.setAnnots(((ListTerm)i.next()).cloneLT());
			}
			return l;
		} catch (Exception e) {
			throw new JasonException("Error creating literal from "+lt);
		}
	}
	
    /** 
     * Transforms this into a full literal (which implements all methods of Literal), if it is an Atom; 
     * otherwise returns 'this'
     */
    public Literal forceFullLiteralImpl() {
        if (this.isAtom() && !(this instanceof LiteralImpl)) 
            return new LiteralImpl(this);
        else 
            return this;
    }
	
	
    @SuppressWarnings("serial")
    static final class TrueLiteral extends Atom {
    	public TrueLiteral() {
    		super("true");
		}

    	/*
    	@Override
        public boolean canBeAddedInBB() {
    		return false;
    	}
    	*/
        
    	@Override
        public Iterator<Unifier> logicalConsequence(final Agent ag, final Unifier un) {
        	return LogExpr.createUnifIterator(un);            
        }
    	
    	/*
    	@Override
    	public Literal clone() {
    	    return this;
    	}*/
    }
    
    @SuppressWarnings("serial")
	static final class FalseLiteral extends Atom {
    	public FalseLiteral() {
    		super("false");
		}

    	/*
    	@Override
        public boolean canBeAddedInBB() {
    		return false;
    	}
    	*/
    	
        @Override
        public Iterator<Unifier> logicalConsequence(final Agent ag, final Unifier un) {
        	return LogExpr.EMPTY_UNIF_LIST.iterator();            
        }
        
        /*
        @Override
        public Literal clone() {
            return this;
        }*/
    }
}
