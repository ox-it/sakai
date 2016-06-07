var require = {
    baseUrl: window.baseUrl || "//static.data.ox.ac.uk/courses-js-widget/", // hack for local testing
    enforceDefine: true,
    paths: {
      "jquery": "lib/jQuery/jquery.min",
      "jquery.dataTables": "lib/dataTables/js/jquery.dataTables.min",
      "moment": "lib/moment/moment.min",
    },
    shim: {
      "jquery": {exports: "$"},
      "jquery.dataTables": {deps: ["jquery"]},
    }
};
