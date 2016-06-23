(ns dashboard.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [ajax.core :refer [GET POST json-response-format]]))

(enable-console-print!)

(def products-list (chan))

(defn handlerProducts [data]
  (println data)
  (go (>! products-list data)))

(defn fetch-products []
  (GET "http://localhost:8080/products" {:handler handlerProducts, :response-format (json-response-format {:keyswords? true})})
  products-list)

(def app-state (atom {:products []}))

(defn display-product [product]
  (let [name (last (first product)) price (last (last product))]
    (str name " - " price)))

(defn product-view [product owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/li nil (display-product product)))))

(defn products-view [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (go (let [products (<! (fetch-products ))]
            (om/update! data :products products)
      )))
    om/IRenderState
    (render-state [this state]
      (dom/div nil
        (dom/h2 nil "Products")
        (apply dom/ul nil
               (om/build-all product-view (:products data)
                             {:init-state state}))))))

(om/root products-view app-state
         {:target (. js/document (getElementById "products"))})
