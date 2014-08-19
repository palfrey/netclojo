(ns netclojo.test.generator
	(:use
		[midje.sweet]
		[slingshot.slingshot :only [throw+ try+]]
	)
	(:require [netclojo.generator :as gen])
)

(fact "Basic setup"
	(gen/parse "to setup ;; comments are written after semicolon(s)
 clear-all ;; clear everything
 create-turtles 10 ;; make 10 new turtles
end ; (one semicolon is enough, but I like two)") => [:S [:line :to :setup] [:line :clear-all] [:line :create-turtles 10] [:line :end]])

(fact "Report"
	(gen/parse "to-report average-wealth ;; this reporter returns the
 report mean [wealth] of turtles ;; average wealth in the
end") => [:S [:line :to-report :average-wealth] [:line :report :mean [:list :wealth] :of :turtles] [:line :end]])

(fact "Basic movement"
	(gen/parse "to go
 ask turtles [
forward 1 ;; all turtles move forward one step
 right random 360 ;; ...and turn a random amount
 ]
end") =>  [:S [:line :to :go] [:line :ask :turtles [:list [:line :forward 1] [:line :right :random 360]]] [:line :end]])

(fact "Report input"
	(gen/parse "to-report absolute-value [number] ;; number is the input
 ifelse number >= 0 ;; if number is already non-negative
 [ report number ] ;; return number (a non-negative value).
 [ report (- number) ] ;; Otherwise, return the opposite, which
end") => [:S
		  [:line :to-report :absolute-value [:list :number]]
		  [:line :ifelse :number [:gte] 0]
		  [:line [:list [:line :report :number]]]
		  [:line [:list [:line :report [:brackets [:minus] :number]]]]
		  [:line :end]])

(fact "globals"
	(gen/parse "globals [ number-of-trees ]") => [:S [:line :globals [:list [:line :number-of-trees]]]]
)

(fact "own"
	(gen/parse "turtles-own [ energy ] ;; each turtle has its own energy
patches-own [ roughness ] ;; each patch has its own roughness
links-own [ strength ] ;; each link has its own strength
") => [:S
	   [:line :turtles-own [:list [:line :energy]]]
	   [:line :patches-own [:list [:line :roughness]]]
	   [:line :links-own [:list [:line :strength]]]])

(fact "let"
	(gen/parse "to swap-colors [turtle1 turtle2] ;; turtle1 and turtle2 are inputs
 let temp ([color] of turtle1) ;; store the color of turtle1 in temp
 ask turtle1 [ set color ([color] of turtle2) ]
;; set turtle1’s color to turtle2’s color
 ask turtle2 [ set color temp ]
;; now set turtle2’s color to turtle1’s (original) color
end ;; (which was conveniently stored in local variable “temp”).")
	  =>  [:S
		   [:line :to :swap-colors [:list :turtle1 :turtle2]]
		   [:line :let :temp [:brackets [:list :color] :of :turtle1]]
		   [:line :ask :turtle1 [:list [:line :set :color [:brackets [:list :color] :of :turtle2]]]]
		   [:line :ask :turtle2 [:list [:line :set :color :temp]]]
		   [:line :end]]
)

(fact "lists"
	(gen/parse "set my-list [2 4 6 8]
set my-random-list list (random 10) (random 20)
show (list random 10)
show (list random 10 random 20 random 30)
set fitness-list ([fitness] of turtles)
;; list containing the fitness of each turtle (in random order)
show [pxcor * pycor] of patches
set my-list [2 7 5 \"Bob\" [3 0 -2]] ;; my-list is now [2 7 5 \"Bob\" [3 0 -2]]
set my-list replace-item 2 my-list 10 ;; my-list is now [2 7 10 \"Bob\" [3 0 -2]]") =>
	  [:S
	   [:line :set :my-list [:list 2 4 6 8]]
	   [:line :set :my-random-list :list [:brackets :random 10] [:brackets :random 20]]
	   [:line :show [:brackets :list :random 10]]
	   [:line :show [:brackets :list :random 10 :random 20 :random 30]]
	   [:line :set :fitness-list [:brackets [:list :fitness] :of :turtles]]
	   [:line :show [:list :pxcor [:multiply] :pycor] :of :patches]
	   [:line :set :my-list [:list 2 7 5 [:string "Bob"] [:list 3 0 -2]]]
	   [:line :set :my-list :replace-item 2 :my-list 10]]
)

(fact "iterate-lists"
	  (gen/parse "foreach [1.2 4.6 6.1] [ show (word ? \" -> \" round ?) ]
show map [round ?] [1.2 2.2 2.7] ;; prints [1 2 3]
show map round [1.2 2.2 2.7] ;; a shorter way (see Tasks in Programming Guide)
show (map [?1 + ?2] [1 2 3] [100 200 300]) ;; prints [101 202 303]
show (map + [1 2 3] [100 200 300]) ;; a shorter way of writing the same") =>
	  [:S
	   [:line :foreach [:list 1.2 4.6 6.1] [:list [:line :show [:brackets :word [:item] [:string " -> "] :round [:item]]]]]
	   [:line :show :map [:list :round [:item]] [:list 1.2 2.2 2.7]]
	   [:line :show :map :round [:list 1.2 2.2 2.7]]
	   [:line :show [:brackets :map [:list [:item 1] [:add] [:item 2]] [:list 1 2 3] [:list 100 200 300]]]
	   [:line :show [:brackets :map [:add] [:list 1 2 3] [:list 100 200 300]]]
	   ]
)

(fact "agentsets"
	  (gen/parse "ask one-of turtles [ set color green ] ;; one-of reports a random agent from an agentset
ask (max-one-of turtles [wealth]) [ donate ] ;; max-one-of agentset [reporter] reports an agent in the agentset that has the highest value for the given reporter
show mean ([wealth] of turtles) ;; Use of to make a list of values, one for each agent in the agentset.
show (turtle-set turtle 0 turtle 2 turtle 9) ;; Use turtle-set, patch-set and link-set reporters to make new agentsets by gathering together agents from a variety of sources
show turtles = patches ;; Check whether two agentsets are equal using = or !=
show member? turtle 0 turtles ;; Use member? to see if an agent is a member of an agentset.
if all? turtles [color = red] ;; use ?all to see if every agent in the
 [ show \"every turtle is red!\" ] ;; agentset satisfies a certain condition
ask turtles [create-links-to other turtles-here ;; on same patch as me, not me,
with [color = [color] of myself] ] ;; and with same color as me.
show [([color] of end1) - ([color] of end2)] of links ;; check everything’s OK") =>
	  [:S
	   [:line :ask :one-of :turtles [:list [:line :set :color :green]]]
	   [:line :ask [:brackets :max-one-of :turtles [:list :wealth]] [:list [:line :donate]]]
	   [:line :show :mean [:brackets [:list :wealth] :of :turtles]]
	   [:line :show [:brackets :turtle-set :turtle 0 :turtle 2 :turtle 9]]
	   [:line :show :turtles [:equal] :patches]
	   [:line :show :member? :turtle 0 :turtles]
	   [:line :if :all? :turtles [:list :color [:equal] :red]]
	   [:line [:list [:line :show [:string "every turtle is red!"]]]]
	   [:line :ask :turtles [:list
							 [:line :create-links-to :other :turtles-here]
							 [:line :with [:list :color [:equal] [:list :color] :of :myself]]]]
	   [:line :show [:list [:brackets [:list :color] :of :end1] [:minus] [:brackets [:list :color] :of :end2]] :of :links]]
)
