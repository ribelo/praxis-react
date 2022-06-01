(ns ribelo.praxis.react
  (:require
   [taoensso.timbre :as timbre]
   [missionary.core :as mi]
   [ribelo.praxis :as px]
   ["react" :as react]))

(def -sentinel (js/Object.))

(defn subscribe
  "[inpure] creates `React/useEffect`, that creates a `listener` and calls the
`set-state!` function on every change.


returns [[reify]], which, when `deref`, returns `state`"
  ([id]
   (subscribe id -sentinel))
  ([id m]
   (let [-m (react/useRef m)
         m' (if (= m (.-current -m)) (.-current -m) m)
         [state set-state!] (react/useState nil)]
     (react/useEffect
      (fn []
        (set! (.-current -m) m)
        (let [<v (px/emit! ::px/listen! id (fn [_ v] (if-not (fn? v) (set-state! v) (set-state! (v m)))))]
          (fn []
            ((mi/sp (when-let [>f (mi/? <v)] (>f))) ; call publisher fn to cancel
             #(timbre/debugf "successful unlisten %s" id %)
             #(timbre/errorf "unsuccessful unlisten %s %s" id %)))))
      (js/Array. (str id) m'))
     (cljs.core/reify
       IDeref
       (-deref [_] state)))))
