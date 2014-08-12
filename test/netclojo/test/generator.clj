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
end") => [:S [:line :to-report :average-wealth] [:line :report :mean [:inputs :wealth] "of" :turtles] [:line :end]])

(fact "Basic movement"
	(gen/parse "to go
 ask turtles [
forward 1 ;; all turtles move forward one step
 right random 360 ;; ...and turn a random amount
 ]
end") =>  [:S [:line :to :go] [:line [:ask :turtles [:line :forward 1] [:line :right :random 360]]] [:line :end]])

(fact "Report input"
	(gen/parse "to-report absolute-value [number] ;; number is the input
 ifelse number >= 0 ;; if number is already non-negative
 [ report number ] ;; return number (a non-negative value).
 [ report (- number) ] ;; Otherwise, return the opposite, which
end") => [:S
		  [:line [:to-report :absolute-value [:inputs :number]]]
		  [:line [:ifelse :number [:gte] 0 [:ifblock [:line :report :number]] [:ifblock [:line :report [:brackets [:minus] :number]]]]]
		  [:line :end]])

(fact "globals"
	(gen/parse "globals [ number-of-trees ]") => [:S [:globals [:inputs :number-of-trees]]]
)

(fact "own"
	(gen/parse "turtles-own [ energy ] ;; each turtle has its own energy
patches-own [ roughness ] ;; each patch has its own roughness
links-own [ strength ] ;; each link has its own strength
") => [:S [:own-thing [:turtles] [:inputs :energy]] [:own-thing [:patches] [:inputs :roughness]] [:own-thing [:links] [:inputs :strength]]])

(fact "let"
	(gen/parse "to swap-colors [turtle1 turtle2] ;; turtle1 and turtle2 are inputs
 let temp ([color] of turtle1) ;; store the color of turtle1 in temp
 ask turtle1 [ set color ([color] of turtle2) ]
;; set turtle1’s color to turtle2’s color
 ask turtle2 [ set color temp ]
;; now set turtle2’s color to turtle1’s (original) color
end ;; (which was conveniently stored in local variable “temp”).")
	  =>  [:S
		   [:line [:to :swap-colors [:inputs :turtle1 :turtle2]]]
		   [:line :let :temp [:brackets [:inputs :color] "of" :turtle1]]
		   [:line [:ask :turtle1 [:line :set :color [:brackets [:inputs :color] "of" :turtle2]]]]
		   [:line [:ask :turtle2 [:line :set :color :temp]]]
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
set my-list replace-item 2 my-list 10 ;; my-list is now [2 7 10 \"Bob\" [3 0 -2]]") => []
)