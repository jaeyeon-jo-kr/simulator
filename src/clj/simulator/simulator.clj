(ns simulator.simulator)

(defn rcomp
  [& coll]
  (->> (reverse coll)
       (apply comp)))

(defn dot-object
  [id]
  {:obj-id id
   :t 0.0
   :x 0.0
   :y 0.0
   :z 0.0
   :update-fn
   (fn [obj {:keys [dt dx dy dz] :as event}]
     (let [update-obj
           (rcomp #(update % :t + dt)
                  #(update % :x + dx)
                  #(update % :y + dy)
                  #(update % :z + dz)
                  #(assoc % :output %))]
       (update-obj obj)))})

(def root-object
  {:obj-id 1
   :t 0.0
   :sub-objs 
   (list (dot-object 2)
     (dot-object 3)) 
   :update-fn
   (fn [obj {:keys [dt] :as event}]
     (let [up-fn
           (rcomp #(update % :t + dt)
                  #(update % :sub-objs
                           (fn [objs]
                             objs
                             (map (fn [{:keys [update-fn] :as obj}]
                                    (update-fn obj {:dt dt
                                                    :dx (* dt 0.1)
                                                    :dy (* dt 0.2)
                                                    :dz (* dt 0.3)})) objs)))
                  #_#(assoc % :output (map :output (:sub-objs %))))]
       (up-fn obj)))})

(comment 
  ((:update-fn root-object) root-object {:dt 0.1})
  )