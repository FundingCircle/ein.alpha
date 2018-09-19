(ns fundingcircle.tools.deps.alpha.extensions.ein
  (:require
   [clojure.java.io :as jio]
   [clojure.string :as str]
   [clojure.tools.deps.alpha.extensions :as ext]
   [clojure.tools.deps.alpha.extensions.pom :as pom]
   [clojure.java.io :as io])
  (:import
   [java.io File IOException]
   [java.net URL]
   [java.util.jar JarFile JarEntry]
   ;; maven-builder-support
   [org.apache.maven.model.building UrlModelSource]))

;; Search up to find the dir instance
(def +ein-root+
  (->> (jio/file ".")
       (.getAbsoluteFile)
       (iterate #(.getParentFile ^File %))
       (filter #(.exists (io/file ^File % "ein")))
       first))

(defn module->root [module-name]
  (let [f (.getAbsoluteFile (io/file +ein-root+ module-name))]
    (when (.exists f)
      (.getPath f))))

(defmethod ext/dep-id :ein
  [lib {:keys [ein/module] :as coord} config]
  {:lib lib
   :root (module->root module)})

(defmethod ext/lib-location :ein
  [lib {:keys [ein/module]} config]
  ;; Modules become local deps with absolute path roots
  {:base (module->root module)
   :path ""
   :type :local})

(defmethod ext/manifest-type :ein
  [lib {:keys [ein/module deps/manifest] :as coord} config]
  (cond
    manifest {:deps/manifest manifest, :deps/root (module->root module)}
    :else (ext/detect-manifest (module->root module))))

(defmethod ext/coord-summary :ein
  [lib {:keys [ein/module]}]
  (str lib " " module))
