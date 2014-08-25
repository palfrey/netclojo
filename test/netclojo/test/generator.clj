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
end ; (one semicolon is enough, but I like two)") => [:S [:to :setup [:statements [:clear-all] [:create-turtles 10]]]])

(fact "Report"
	(gen/parse "to-report average-wealth ;; this reporter returns the
 report mean [wealth] of turtles ;; average wealth in the
end") => [:S [:to-report :average-wealth [:statements [:report :mean [:wealth] :of :turtles]]]])

(fact "Basic movement"
	(gen/parse "to go
 ask turtles [
forward 1 ;; all turtles move forward one step
 right random 360 ;; ...and turn a random amount
 ]
end") =>  [:S
		   [:to :go [:statements
		   [:ask :turtles [:statements [:forward 1] [:right :random 360]]]
	  ]]])

(fact "Report input"
	(gen/parse "to-report absolute-value [number] ;; number is the input
 ifelse number >= 0 ;; if number is already non-negative
 [ report number ] ;; return number (a non-negative value).
 [ report (- number) ] ;; Otherwise, return the opposite, which
end") => [:S
		  [:to-report :absolute-value [:number] [:statements
		  [:ifelse :number [:gte] 0
			  [:statements [:report :number]]
			  [:statements [:report [:brackets [:minus] :number]]]]
		  ]]])

(fact "globals"
	(gen/parse "globals [ number-of-trees ]") => [:S [:globals [:number-of-trees]]]
)

(fact "own"
	(gen/parse "turtles-own [ energy ] ;; each turtle has its own energy
patches-own [ roughness ] ;; each patch has its own roughness
links-own [ strength ] ;; each link has its own strength
") => [:S
	   [:own :turtles [:energy]]
	   [:own :patches [:roughness]]
	   [:own :links [:strength]]])

(fact "let"
	(gen/parse "to swap-colors [turtle1 turtle2] ;; turtle1 and turtle2 are inputs
 let temp ([color] of turtle1) ;; store the color of turtle1 in temp
 ask turtle1 [ set color ([color] of turtle2) ]
;; set turtle1’s color to turtle2’s color
 ask turtle2 [ set color temp ]
;; now set turtle2’s color to turtle1’s (original) color
end ;; (which was conveniently stored in local variable “temp”).")
	  =>  [:S
		   [:to :swap-colors [:turtle1 :turtle2] [:statements
		   [:let :temp [:brackets [:color] :of :turtle1]]
		   [:ask :turtle1 [:statements [:set :color [:brackets [:color] :of :turtle2]]]]
		   [:ask :turtle2 [:statements [:set :color :temp]]]
		   ]]]
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
	   [:set :my-list [2 4 6 8]]
	   [:set :my-random-list :list [:brackets :random 10] [:brackets :random 20]]
	   [:show [:brackets :list :random 10]]
	   [:show [:brackets :list :random 10 :random 20 :random 30]]
	   [:set :fitness-list [:brackets [:fitness] :of :turtles]]
	   [:show [:pxcor [:multiply] :pycor] :of :patches]
	   [:set :my-list [2 7 5 "Bob" [3 0 -2]]]
	   [:set :my-list :replace-item 2 :my-list 10]]
)

(fact "iterate-lists"
	  (gen/parse "foreach [1.2 4.6 6.1] [ show (word ? \" -> \" round ?) ]
show map [round ?] [1.2 2.2 2.7] ;; prints [1 2 3]
show map round [1.2 2.2 2.7] ;; a shorter way (see Tasks in Programming Guide)
show (map [?1 + ?2] [1 2 3] [100 200 300]) ;; prints [101 202 303]
show (map + [1 2 3] [100 200 300]) ;; a shorter way of writing the same") =>
	  [:S
	   [:foreach [1.2 4.6 6.1]
			   [:statements [:show [:brackets :word [:item] " -> " :round [:item]]]]]
	   [:show :map [:round [:item]] [1.2 2.2 2.7]]
	   [:show :map :round [1.2 2.2 2.7]]
	   [:show [:brackets :map [[:item 1] [:add] [:item 2]] [1 2 3] [100 200 300]]]
	   [:show [:brackets :map [:add] [1 2 3] [100 200 300]]]]
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
	   [:ask :one-of :turtles [:statements [:set :color :green]]]
	   [:ask [:brackets :max-one-of :turtles [:wealth]] [:statements [:donate]]]
	   [:show :mean [:brackets [:wealth] :of :turtles]]
	   [:show [:brackets :turtle-set :turtle 0 :turtle 2 :turtle 9]]
	   [:show :turtles [:equal] :patches]
	   [:show :member? :turtle 0 :turtles]
	   [:if :all? :turtles [:color [:equal] :red]]
	   [:show "every turtle is red!"]
	   [:ask :turtles
			   [:statements [:create-links-to :other :turtles-here]
				[:with [:color [:equal] [:color] :of :myself]]]]
	   [:show [[:brackets [:color] :of :end1] [:minus] [:brackets [:color] :of :end2]] :of :links]]
)

(fact "sync"
	  (gen/parse "ask turtles
 [ forward random 10 ;; move forward a random number of steps (0–9)
 wait 0.5 ;; wait half a second
 set color blue ] ;; set your color to blue

ask turtles [ forward random 10 ]
ask turtles [ wait 0.5 ] ;; note that you
ask turtles [ set color blue ] ;; (0.5 * number-of-turtles) seconds

set my-list-of-agents sort-by [[size] of ?1 < [size] of ?2] turtles
;; This sets my-list-of-agents to a list of turtles sorted in
;; ascending order by their turtle variable size.
foreach my-list-of-agents [
 ask ? [ ;; each agent undertakes the list of commands
 forward random 10 ;; (forward, wait, and set) without being
 wait 0.5 ;; interrupted, i.e. the next agent does not
 set color blue ;; start until the previous one has finished.
]]") =>
	  [:S
	   [:ask :turtles [:statements [:forward :random 10] [:wait 0.5] [:set :color :blue]]]
	   [:ask :turtles [:statements [:forward :random 10]]]
	   [:ask :turtles [:statements [:wait 0.5]]]
	   [:ask :turtles [:statements [:set :color :blue]]]
	   [:set :my-list-of-agents :sort-by [[:size] :of [:item 1] [:lt] [:size] :of [:item 2]] :turtles]
	   [:foreach :my-list-of-agents
			   [:statements [:ask [:item]
										[:statements [:forward :random 10]
										 [:wait 0.5]
										 [:set :color :blue]]]]]]
	  )
