! function() {
    function c(a, c, d) {
        function e(e, k) {
            var l = k.dirPaginate,
                m = l.match(/^\s*([\s\S]+?)\s+in\s+([\s\S]+?)(?:\s+as\s+([\s\S]+?))?(?:\s+track\s+by\s+([\s\S]+?))?\s*$/),
                n = /\|\s*itemsPerPage\s*:\s*(.*\(\s*\w*\)|([^\)]*?(?=\s+as\s+))|[^\)]*)/;
            if (null === m[2].match(n)) throw "pagination directive: the 'itemsPerPage' filter must be set.";
            var o = m[2].replace(n, ""),
                p = c(o);
            h(e);
            var q = k.paginationId || b;
            return d.registerInstance(q),
                function(h, k, m) {
                    var n = c(m.paginationId)(h) || m.paginationId || b;
                    d.registerInstance(n);
                    var o = f(l, n);
                    g(k, m, o), i(k);
                    var q = a(k),
                        r = j(h, m, n);
                    d.setCurrentPageParser(n, r, h), "undefined" != typeof m.totalItems ? (d.setAsyncModeTrue(n), h.$watch(function() {
                        return c(m.totalItems)(h)
                    }, function(a) {
                        0 <= a && d.setCollectionLength(n, a)
                    })) : (d.setAsyncModeFalse(n), h.$watchCollection(function() {
                        return p(h)
                    }, function(a) {
                        if (a) {
                            var b = a instanceof Array ? a.length : Object.keys(a).length;
                            d.setCollectionLength(n, b)
                        }
                    })), q(h)
                }
        }

        function f(a, c) {
            var d, e = !!a.match(/(\|\s*itemsPerPage\s*:[^|]*:[^|]*)/);
            return d = c === b || e ? a : a.replace(/(\|\s*itemsPerPage\s*:\s*[^|\s]*)/, "$1 : '" + c + "'")
        }

        function g(a, b, c) {
            a[0].hasAttribute("dir-paginate-start") || a[0].hasAttribute("data-dir-paginate-start") ? (b.$set("ngRepeatStart", c), a.eq(a.length - 1).attr("ng-repeat-end", !0)) : b.$set("ngRepeat", c)
        }

        function h(a) {
            angular.forEach(a, function(a) {
                1 === a.nodeType && angular.element(a).attr("dir-paginate-no-compile", !0)
            })
        }

        function i(a) {
            angular.forEach(a, function(a) {
                1 === a.nodeType && angular.element(a).removeAttr("dir-paginate-no-compile")
            }), a.eq(0).removeAttr("dir-paginate-start").removeAttr("dir-paginate").removeAttr("data-dir-paginate-start").removeAttr("data-dir-paginate"), a.eq(a.length - 1).removeAttr("dir-paginate-end").removeAttr("data-dir-paginate-end")
        }

        function j(a, b, d) {
            var e;
            if (b.currentPage) e = c(b.currentPage);
            else {
                var f = (d + "__currentPage").replace(/\W/g, "_");
                a[f] = 1, e = c(f)
            }
            return e
        }
        return {
            terminal: !0,
            multiElement: !0,
            priority: 100,
            compile: e
        }
    }

    function d() {
        return {
            priority: 5e3,
            terminal: !0
        }
    }

    function e(a) {
        a.put("angularUtils.directives.dirPagination.template", '<ul class="pagination" ng-if="1 < pages.length || !autoHide"><li ng-if="boundaryLinks" ng-class="{ disabled : pagination.current == 1 }"><a href="" ng-click="setCurrent(1)">First</a></li><li ng-if="directionLinks" ng-class="{ disabled : pagination.current == 1 }"><a href="" ng-click="setCurrent(pagination.current - 1)">Prev</a></li><li ng-repeat="pageNumber in pages track by tracker(pageNumber, $index)" ng-class="{ active : pagination.current == pageNumber, disabled : pageNumber == \'...\' || ( ! autoHide && pages.length === 1 ) }"><a href="" ng-click="setCurrent(pageNumber)">{{ pageNumber }}</a></li><li ng-if="directionLinks" ng-class="{ disabled : pagination.current == pagination.last }"><a href="" ng-click="setCurrent(pagination.current + 1)">Next</a></li><li ng-if="boundaryLinks"  ng-class="{ disabled : pagination.current == pagination.last }"><a href="" ng-click="setCurrent(pagination.last)">Last</a></li></ul>')
    }

    function f(a, c) {
        function g(c, e, f) {
            function l(b) {
                if (a.isRegistered(i) && o(b)) {
                    var d = c.pagination.current;
                    c.pages = h(b, a.getCollectionLength(i), a.getItemsPerPage(i), k), c.pagination.current = b, n(), c.onPageChange && c.onPageChange({
                        newPageNumber: b,
                        oldPageNumber: d
                    })
                }
            }

            function m() {
                if (a.isRegistered(i)) {
                    var b = parseInt(a.getCurrentPage(i)) || 1;
                    c.pages = h(b, a.getCollectionLength(i), a.getItemsPerPage(i), k), c.pagination.current = b, c.pagination.last = c.pages[c.pages.length - 1], c.pagination.last < c.pagination.current ? c.setCurrent(c.pagination.last) : n()
                }
            }

            function n() {
                if (a.isRegistered(i)) {
                    var b = a.getCurrentPage(i),
                        d = a.getItemsPerPage(i),
                        e = a.getCollectionLength(i);
                    c.range.lower = (b - 1) * d + 1, c.range.upper = Math.min(b * d, e), c.range.total = e
                }
            }

            function o(a) {
                return d.test(a) && 0 < a && a <= c.pagination.last
            }
            var g = f.paginationId || b,
                i = c.paginationId || f.paginationId || b;
            if (!a.isRegistered(i) && !a.isRegistered(g)) {
                var j = i !== b ? " (id: " + i + ") " : " ";
                window.console && console.warn("Pagination directive: the pagination controls" + j + "cannot be used without the corresponding pagination directive, which was not found at link time.")
            }
            c.maxSize || (c.maxSize = 9), c.autoHide = void 0 === c.autoHide || c.autoHide, c.directionLinks = !angular.isDefined(f.directionLinks) || c.$parent.$eval(f.directionLinks), c.boundaryLinks = !!angular.isDefined(f.boundaryLinks) && c.$parent.$eval(f.boundaryLinks);
            var k = Math.max(c.maxSize, 5);
            c.pages = [], c.pagination = {
                last: 1,
                current: 1
            }, c.range = {
                lower: 1,
                upper: 1,
                total: 1
            }, c.$watch("maxSize", function(a) {
                a && (k = Math.max(c.maxSize, 5), m())
            }), c.$watch(function() {
                if (a.isRegistered(i)) return (a.getCollectionLength(i) + 1) * a.getItemsPerPage(i)
            }, function(a) {
                0 < a && m()
            }), c.$watch(function() {
                if (a.isRegistered(i)) return a.getItemsPerPage(i)
            }, function(a, b) {
                a != b && "undefined" != typeof b && l(c.pagination.current)
            }), c.$watch(function() {
                if (a.isRegistered(i)) return a.getCurrentPage(i)
            }, function(a, b) {
                a != b && l(a)
            }), c.setCurrent = function(b) {
                a.isRegistered(i) && o(b) && (b = parseInt(b, 10), a.setCurrentPage(i, b))
            }, c.tracker = function(a, b) {
                return a + "_" + b
            }
        }

        function h(a, b, c, d) {
            var h, e = [],
                f = Math.ceil(b / c),
                g = Math.ceil(d / 2);
            h = a <= g ? "start" : f - g < a ? "end" : "middle";
            for (var j = d < f, k = 1; k <= f && k <= d;) {
                var l = i(k, a, d, f),
                    m = 2 === k && ("middle" === h || "end" === h),
                    n = k === d - 1 && ("middle" === h || "start" === h);
                j && (m || n) ? e.push("...") : e.push(l), k++
            }
            return e
        }

        function i(a, b, c, d) {
            var e = Math.ceil(c / 2);
            return a === c ? d : 1 === a ? a : c < d ? d - e < b ? d - c + a : e < b ? b - e + a : a : a
        }
        var d = /^\d+$/,
            e = {
                restrict: "AE",
                scope: {
                    maxSize: "=?",
                    onPageChange: "&?",
                    paginationId: "=?",
                    autoHide: "=?"
                },
                link: g
            },
            f = c.getString();
        return void 0 !== f ? e.template = f : e.templateUrl = function(a, b) {
            return b.templateUrl || c.getPath()
        }, e
    }

    function g(a) {
        return function(c, d, e) {
            if ("undefined" == typeof e && (e = b), !a.isRegistered(e)) throw "pagination directive: the itemsPerPage id argument (id: " + e + ") does not match a registered pagination-id.";
            var f, g;
            if (angular.isObject(c)) {
                if (d = parseInt(d) || 9999999999, g = a.isAsyncMode(e) ? 0 : (a.getCurrentPage(e) - 1) * d, f = g + d, a.setItemsPerPage(e, d), c instanceof Array) return c.slice(g, f);
                var i = {};
                return angular.forEach(h(c).slice(g, f), function(a) {
                    i[a] = c[a]
                }), i
            }
            return c
        }
    }

    function h(a) {
        if (Object.keys) return Object.keys(a);
        var b = [];
        for (var c in a) a.hasOwnProperty(c) && b.push(c);
        return b
    }

    function i() {
        var b, a = {};
        this.registerInstance = function(c) {
            "undefined" == typeof a[c] && (a[c] = {
                asyncMode: !1
            }, b = c)
        }, this.deregisterInstance = function(b) {
            delete a[b]
        }, this.isRegistered = function(b) {
            return "undefined" != typeof a[b]
        }, this.getLastInstanceId = function() {
            return b
        }, this.setCurrentPageParser = function(b, c, d) {
            a[b].currentPageParser = c, a[b].context = d
        }, this.setCurrentPage = function(b, c) {
            a[b].currentPageParser.assign(a[b].context, c)
        }, this.getCurrentPage = function(b) {
            var c = a[b].currentPageParser;
            return c ? c(a[b].context) : 1
        }, this.setItemsPerPage = function(b, c) {
            a[b].itemsPerPage = c
        }, this.getItemsPerPage = function(b) {
            return a[b].itemsPerPage
        }, this.setCollectionLength = function(b, c) {
            a[b].collectionLength = c
        }, this.getCollectionLength = function(b) {
            return a[b].collectionLength
        }, this.setAsyncModeTrue = function(b) {
            a[b].asyncMode = !0
        }, this.setAsyncModeFalse = function(b) {
            a[b].asyncMode = !1
        }, this.isAsyncMode = function(b) {
            return a[b].asyncMode
        }
    }

    function j() {
        var b, a = "angularUtils.directives.dirPagination.template";
        this.setPath = function(b) {
            a = b
        }, this.setString = function(a) {
            b = a
        }, this.$get = function() {
            return {
                getPath: function() {
                    return a
                },
                getString: function() {
                    return b
                }
            }
        }
    }
    var a = "angularUtils.directives.dirPagination",
        b = "__default";
    angular.module(a, []).directive("dirPaginate", ["$compile", "$parse", "paginationService", c]).directive("dirPaginateNoCompile", d).directive("dirPaginationControls", ["paginationService", "paginationTemplate", f]).filter("itemsPerPage", ["paginationService", g]).service("paginationService", i).provider("paginationTemplate", j).run(["$templateCache", e])
}();