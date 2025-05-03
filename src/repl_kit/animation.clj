(ns repl-kit.animation
  (:require
   [seesaw.core :refer [invoke-later]]
   [clojure.core.async :refer [<! chan <! >! timeout close! go-loop]]))

(defn coll->chan
  "Returns a core.async channel which cycles through the supplied sequence of [delay data] pairs.
  The channel will cycle through each pair, delaying the specified number of milliseconds,
  and then emits the data onto the channel. The channel will cycle until it is closed."
  [coll]
  (let [ch (chan)]
    (go-loop
     [coll (cycle coll)]
      (when-let [[delay data] (first coll)]
        (<! (timeout delay))
        (>! ch data)
        (recur (rest coll))))
    ch))

(defn busy-animation
  "Returns a core.async channel which cycles through the supplied frames of [delay data]
  pairs. After each delay, the supplied fn is invoked with the data portion of the frame.
  This will continue indefinitely until the channel is closed."
  [frames f]
  (let [ch (coll->chan frames)]
    (go-loop
     []
      (when-let [text (<! ch)]
        (f text)
        (recur)))
    ch))

(def execute-frames
  [[180 "*xecuting"]
   [180 "e*ecuting"]
   [180 "ex*cuting"]
   [180 "exe*uting"]
   [180 "exec*ting"]
   [180 "execu*ing"]
   [180 "execut*ng"]
   [180 "executi*g"]
   [180 "executin*"]])

(def spinner-frames 
  [[180 "/"] 
   [180 "-"] 
   [180 "\\"] 
   [180 "|"]])

(defn mk-label-animation 
  "Create a closure that binds a label to an animation channel. 
   Returns a fn that accepts the operations :start and :stop"
 [label frames] 
 (let [ch  (atom nil)
       ops {:start (fn [] 
                     ;; TODO, close the channel here to prevent
                     ;; channel leak when start an animation when
                     ;; one is currently in progress. consider showing a count
                     (reset! ch (busy-animation 
                                 frames 
                                 (fn [txt]
                                   (invoke-later (.setText label txt))))))
            :stop  (fn [] 
                     (close! @ch)
                     (invoke-later (.setText label "")))}]
   (fn [operation & args] 
     (-> (ops operation) (apply args)))))

(comment 
  (def ch (busy-animation execute-frames #(prn %)))
  
  (close! ch)
  ;;
  )