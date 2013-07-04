'use strict';
define(function(require, exports) {

var expertEditTemp = require("../templates/expert/edit.html");
var expertRulesTemp = require("../templates/expert/rules.html");
var expertOperatorsTemp = require("../templates/expert/operators.html");

angular.module('ecgExpert', [])
.controller('ExpertController', ['$scope', '$filter', '$timeout', '$location', 'EnumService', 'ExpertService', function ($scope, $filter, $timeout, $location, EnumService, ExpertService) {
    // 表格头
    $scope.subheader.title = "专家";

    // 命名空间
    $scope.expert = {};

    // 表格展示
    $scope.expert.data = ExpertService.queryAll();
    $scope.expert.filteredData = $scope.expert.data;

    // 显示label
    $scope.expert.getGenderLabel = function(expert) {
        return EnumService.getGenderLabel(expert.gender);
    };
    $scope.expert.getDismissedLabel = function(expert) {
        return EnumService.getDismissedLabel(expert.dismissed);
    };

    // 当前选中数据
    $scope.expert.selectedItem = null;

    // 刷新功能
    function refreshGrid() {
        $scope.expert.data = ExpertService.queryAll();
        $scope.expert.filteredData = $scope.expert.data;
    }

    // 删除功能
    $scope.expert.confirmDelete = function() {
        var selectedItem = $scope.expert.selectedItem;
        if (selectedItem === null) {
            $scope.dialog.alert({
                text: '请选择一条记录!'
            });
            return;
        }
        $scope.dialog.confirm({
            text: "请确认删除专家:" + selectedItem.name + ", 该操作无法恢复!",
            handler: function() {
                $scope.dialog.showStandby();
                ExpertService.remove(selectedItem.id)
                .then(function() {
                    $scope.dialog.hideStandby();
                    $scope.expert.selectedItem = null;
                    $scope.message.success("删除成功!");
                    // 刷新
                    refreshGrid();
                }, function() {
                    $scope.dialog.hideStandby();
                    $scope.message.error("无法删除该数据,可能是您的权限不足,请联系管理员!");
                });
            }
        });
    };

    // 过滤功能
    $scope.expert.queryChanged = function(query) {
        return $scope.expert.filteredData = $filter("filter")($scope.expert.data, query);
    };

    // 编辑功能
    $scope.expert.showPage = function(expert) {
        $location.path("expert/" + expert.id);
    };

}])
.controller('ExpertNewController', ['$scope', '$timeout', '$location', 'EnumService', 'ProfileService', 'ExpertService',
    function ($scope, $timeout, $location, EnumService, ProfileService, ExpertService) {
    $scope.subheader.title = "新增专家";

    $scope.expert = {};
    $scope.expert.newobj = ExpertService.getPlainObject();
    $scope.expert.genders = EnumService.getGenders();
    $scope.expert.dismissedStates = EnumService.getDismissedStates();

    $('#expert-birthday').datetimepicker({
        format: "yyyy-MM-dd",
        language: "zh-CN",
        pickTime: false
    }).on('changeDate', function(e) {
        $scope.expert.newobj.birthday = $('#expert-birthday input').val();
    });

    $scope.expert.showDatePicker = function() {
        $('#expert-birthday').datetimepicker('show');
    };

    $scope.expert.isUnique = true;
    $scope.expert.checkUnique = function() {
        if (!$scope.expert.newobj.username) { return; }
        ProfileService.get($scope.expert.newobj.username).then(function(user) {
            if (user) { 
                $scope.expert.isUnique = false;
                $scope.message.warn("用户" + $scope.expert.newobj.username + "已存在!");
            } else {
                $scope.expert.isUnique = true;
            }
        }, function() {
            $scope.expert.isUnique = true;
            $scope.message.warn("查询用户是否唯一时出错!");
        });
    };

    $scope.expert.create = function() {
        $scope.dialog.showStandby();
        $scope.expert.newobj.birthday = $('#expert-birthday input').val();
        $scope.expert.newobj.password = $scope.expert.newobj.username;
        ExpertService.create($scope.expert.newobj)
        .then(function(result) {
            $scope.dialog.hideStandby();
            if (result) {
                $scope.message.success("新增成功!");
                $location.path("/expert");
            } else {
                $scope.message.error("新增失败!");
            }
        }, function() {
            $scope.dialog.hideStandby();
            $scope.message.error("服务器异常,新增失败!");
        });;
    };
}])
.controller('ExpertViewController', ['$scope', '$routeParams', '$timeout', '$location', 'EnumService', 'ExpertService',
    function ($scope, $routeParams, $timeout, $location, EnumService, ExpertService) {
    $scope.subheader.title = "编辑专家";

    $scope.expert = {};
    $scope.expert.tab = 1; // 默认为基本页面

}])
// 基本信息
.controller('ExpertEditController', ['$scope', '$routeParams', '$timeout', '$location', 'EnumService', 'ProfileService', 'ExpertService',
    function ($scope, $routeParams, $timeout, $location, EnumService, ProfileService, ExpertService) {
    $scope.expert.updateobj = null; //ExpertService.get($routeParams.id);

    // 初始化界面,并获得最新version
    function refresh() {
        ExpertService.get($routeParams.id).then(function(expert) {
            $scope.expert.updateobj = expert;
        });
    };
    refresh();

    $scope.expert.genders = EnumService.getGenders();
    $scope.expert.dismissedStates = EnumService.getDismissedStates();

    $('#expert-birthday').datetimepicker({
        format: "yyyy-MM-dd",
        language: "zh-CN",
        pickTime: false,
    }).on('changeDate', function(e) {
        $scope.expert.updateobj.birthday = $('#expert-birthday input').val();
    });

    $scope.expert.showDatePicker = function() {
        $('#expert-birthday').datetimepicker('show');
    };

    $scope.expert.update = function() {
        $scope.dialog.showStandby();
        $scope.expert.updateobj.birthday = $('#expert-birthday input').val();
        ExpertService.update($scope.expert.updateobj)
        .then(function(result) {
            $scope.dialog.hideStandby();
            $scope.message.success("编辑成功!");
            refresh();
        }, function() {
            $scope.dialog.hideStandby();
            $scope.message.error("编辑失败!");
        });;
    };

    $scope.expert.resetPassword = function() {
        $scope.dialog.confirm({
            text: "重置后登录密码将于登录名一致，确定继续?",
            handler: function() {
                $scope.dialog.showStandby();
                ProfileService.resetPassword($scope.expert.updateobj.id)
                .then(function(result) {
                    $scope.dialog.hideStandby();
                    $scope.message.success("重置密码成功!");
                    refresh();
                }, function() {
                    $scope.dialog.hideStandby();
                    $scope.message.error("重置密码失败!");
                });
            }
        });
    };
}])
.directive("ecgExpertEdit", [ '$location', function($location) {
    return {
        restrict : 'A',
        replace : false,
        template : expertEditTemp,
        controller : "ExpertEditController",
        link : function($scope, $element, $attrs) {
        }
    };
} ])
// 自定义规则
.controller('ExpertRulesController', ['$scope', '$routeParams', '$timeout', '$location', 'EnumService', 'ExpertService',
    function ($scope, $routeParams, $timeout, $location, EnumService, ExpertService) {
    $scope.expert.rules = ExpertService.getRules($routeParams.id);

    $scope.expert.updateRules = function() {
        $scope.dialog.showStandby();
        /* TODO: */
        // ExpertService.update($scope.expert.updateobj);
        $timeout(function() {
            $scope.dialog.hideStandby();
            $scope.message.success("修改自定义规则成功!");
        }, 2000);
    };
}])
.directive("ecgExpertRules", [ '$location', function($location) {
    return {
        restrict : 'A',
        replace : false,
        template : expertRulesTemp,
        controller : "ExpertRulesController",
        link : function($scope, $element, $attrs) {
        }
    };
} ])
// 配置接线员
.controller('ExpertOperatorsController', ['$scope', '$routeParams', '$timeout', '$location', 'EnumService', 'ExpertService',
    function ($scope, $routeParams, $timeout, $location, EnumService, ExpertService) {
    $scope.expert.operators = ExpertService.getOperators($routeParams.id);

    $scope.expert.updateOperators = function() {
        $scope.dialog.showStandby();
        /* TODO: */
        // ExpertService.update($scope.expert.updateobj);
        $timeout(function() {
            $scope.dialog.hideStandby();
            $scope.message.success("配置接线员成功!");
        }, 2000);
    };
}])
.directive("ecgExpertOperators", [ '$location', function($location) {
    return {
        restrict : 'A',
        replace : false,
        template : expertOperatorsTemp,
        controller : "ExpertOperatorsController",
        link : function($scope, $element, $attrs) {
        }
    };
}]);


});