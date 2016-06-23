(ns dashboard.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [ajax.core :refer [GET POST]]))

(enable-console-print!)

(def products-list (chan))

(defn handlerProduct [data]
  (go (>! products-list data)))

(defn fetch-something []
  (GET "http://localhost:8080/products" {:handler handlerProduct})
  products-list)

(defn display-name [data]
  (str data))

(def app-state (atom {:products [10 20]}))

(defn product-view [data owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div nil
        (dom/h2 nil (display-name data))))))

(defn products-view [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:delete (chan)
       :text ""})
    om/IWillMount
    (will-mount [_]
      (go (let [foo (<! (fetch-something ))]
            (om/update! data :products foo)
      )))
    om/IRenderState
    (render-state [this state]
      (dom/div nil
        (dom/h2 nil "Products List")
        (apply dom/ul nil
               (om/build-all product-view (:products data)
                             {:init-state state}))))))

(om/root products-view app-state
         {:target (. js/document (getElementById "products"))})
