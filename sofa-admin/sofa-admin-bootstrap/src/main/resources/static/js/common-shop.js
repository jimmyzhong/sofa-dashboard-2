Date.prototype.Format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "H+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}
if($.validator){
    $.validator.addMethod(
        "reg", //验证方法名称
        function(value, element, param) {//验证规则
            return  param.test(value);
        },
        '您输入的数据格式不对'//验证提示信息
    );
}
$(function () {
    // autocomplete="off"
    $("input").attr("autocomplete", "off");
})

var SHOP_COMMON = {
    pageBack:function(){
        var topWindow = $(window.parent.document);
        var currentId = $('.page-tabs-content', topWindow).find('.active').attr('data-panel');
        var $contentWindow = $('.RuoYi_iframe[data-id="' + currentId + '"]', topWindow)[0].contentWindow;
        $contentWindow.$(".layui-layer-padding").removeAttr("style");
        if ($contentWindow.table.options.type == table_type.bootstrapTable) {
            $contentWindow.$.table.refresh();
        } else if ($contentWindow.table.options.type == table_type.bootstrapTreeTable) {
            $contentWindow.$.treeTable.refresh();
        }
        $.modal.closeTab();
    },
    test: {
        isURL: function isURL(str_url) {// 验证url
            var strRegex = "^((https|http|ftp|rtsp|mms)?://)"
                + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" // ftp    的user@
                + "(([0-9]{1,3}\.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184
                + "|" // 允许IP和DOMAIN（域名）
                + "([0-9a-z_!~*'()-]+\.)*" // 域名- www.
                + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\." // 二级域名
                + "[a-z]{2,6})" // first level domain- .com or .museum
                + "(:[0-9]{1,4})?" // 端口- :80
                + "((/?)|" // a slash isn't required if there is no file name
                + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";
            var re = new RegExp(strRegex);
            return re.test(str_url);
        }
    },
    ajax: {
        //formdata图片上传的参数，sucBack成功的回调，
        uploadImg: function (formdataOrigin, sucBack) {
            ImgBase64(formdataOrigin.get("file"), 1024, function(base64Data){
                var formdata = new FormData();
                formdata.append("file",dataURLtoBlob(base64Data));
                $.modal.loading("图片上传中");
                $.ajax({
                    url: ctx + "ext/shop/oss/upload",
                    data: formdata,
                    type: "post",
                    processData: false,
                    contentType: false,
                    success: function (result) {
                        $.modal.closeLoading();
                        if (result.code == web_status.SUCCESS) {
                            sucBack(result)
                        } else {
                            $.modal.alertError(result.msg);
                        }
                    },
                    error: function (err) {
                        $.modal.closeLoading();
                        $.modal.alertError("图片上传失败" + err.status);
                    }
                })
            });
        },
        uploadFile: function (formdata, sucBack) {
            $.modal.loading("文件上传中");
            $.ajax({
                url: ctx + "ext/shop/oss/file",
                data: formdata,
                type: "post",
                processData: false,
                contentType: false,
                success: function (result) {
                    $.modal.closeLoading();
                    if (result.code == web_status.SUCCESS) {
                        sucBack(result)
                    } else {
                        $.modal.alertError(result.msg);
                    }
                },
                error: function (err) {
                    $.modal.closeLoading();
                    $.modal.alertError("文件上传失败" + err.status);
                }
            })
        },
        get: function (url, data, sucBack) {
            $.modal.loading("数据获取中");
            $.ajax({
                url: url,
                type: "get",
                success: function (result) {
                    $.modal.closeLoading();
                    if (result.code == web_status.SUCCESS) {
                        sucBack(result)
                    } else {
                        $.modal.alertError(result.msg);
                    }
                },
                error: function (err) {
                    $.modal.closeLoading();
                    $.modal.alertError("数据请求失败" + err.status);
                }
            })
        },
        post: function (url, data, sucBack) {
            $.modal.loading("请求中");
            $.ajax({
                url: url,
                type: "post",
                data: data,
                processData: false,
                contentType: false,
                success: function (result) {
                    $.modal.closeLoading();
                    if (result.code == web_status.SUCCESS) {
                        sucBack(result)
                    } else {
                        $.modal.alertError(result.msg);
                    }
                },
                error: function (err) {
                    $.modal.closeLoading();
                    $.modal.alertError("数据请求失败" + err.status);
                }
            })
        },
        form: function (url, data, sucBack) {
            var sendData = {}
            for (var i = 0; i < data.length; i++) {
                sendData[data[i].name] = data[i].value;
            }
            console.log(JSON.stringify(sendData))
            $.modal.loading("数据提交中");
            $.ajax({
                cache: true,
                url: url,
                type: "POST",
                data: sendData,
                async: false,
                success: function (result) {
                    $.modal.closeLoading();
                    if (result.code == web_status.SUCCESS) {
                        sucBack(result)
                    } else {
                        $.modal.alertError(result.msg);
                    }
                },
                error: function (err) {
                    $.modal.closeLoading();
                    $.modal.alertError("系统错误");
                }
            })
        },
        pageSave: function (url, data, sucBack) {
            var sendData = {}
            for (var i = 0; i < data.length; i++) {
                sendData[data[i].name] = data[i].value;
            }
            $.modal.loading("数据提交中");
            $.ajax({
                cache: true,
                url: url,
                type: "POST",
                data: sendData,
                async: false,
                success: function (result) {
                    $.modal.closeLoading();
                    if (result.code == web_status.SUCCESS) {
                        var topWindow = $(window.parent.document);
                        var currentId = $('.page-tabs-content', topWindow).find('.active').attr('data-panel');
                        console.log("currentId",currentId)
                        if(currentId != null && currentId.indexOf("detail")!=-1){
                            var target = $('.RuoYi_iframe[data-id="' + currentId + '"]', topWindow)
                            var url = target.attr('src');
                            target.attr('src', url).load(function () {
                                $.modal.closeTab();
                            });
                        }else{
                            $.operate.successTabCallback(result);
                        }
                    } else {
                        $.modal.alertError(result.msg);
                    }
                },
                error: function (err) {
                    $.modal.closeLoading();
                    $.modal.alertError("系统错误");
                }
            })
        },
    }
}

function ImgBase64(file, maxWidth, backData){
    /*
     * file:input上传图片
     * backData：处理完成回调函数
     * */
    var reader = new FileReader();
    var image = new Image();
    var canvas = createCanvas();
    var ctx = canvas.getContext("2d");
    reader.onload = function(){ // 文件加载完处理
        var result = this.result;
        image.onload = function(){ // 图片加载完处理
            var imgScale = imgScaleW(maxWidth,this.width,this.height);
            canvas.width = imgScale.width;
            canvas.height = imgScale.height;
            ctx.drawImage(image,0,0,imgScale.width,imgScale.height);
            var dataURL = canvas.toDataURL('image/jpeg'); // 图片base64
            ctx.clearRect(0,0,imgScale.width,imgScale.height); // 清除画布
            backData (dataURL); //dataURL:处理成功返回的图片base64
        }
        image.src = result;
    };
    reader.readAsDataURL(file);
}

function createCanvas(){ // 创建画布
    var canvas = document.getElementById('canvas');
    if(!canvas){
        var canvasTag = document.createElement('canvas');
        canvasTag.setAttribute('id','canvas');
        canvasTag.setAttribute('style','display:none;');//隐藏画布
        document.body.appendChild(canvasTag);
        canvas = document.getElementById('canvas');
    }
    return canvas;
}

function imgScaleW(maxWidth,width,height){
    /* maxWidth:宽度或者高度最大值
     * width：宽度
     * height：高度
     * */
    var imgScale = {};
    var w = 0;
    var h = 0;
    if(maxWidth == 0 || (width <= maxWidth && height <= maxWidth)){
        // 如果限制宽度为 0 或图片宽高都小于限制的最大值,不用缩放
        imgScale = {
            width:width,
            height:height
        }
    }else{
        if(width >= height){ // 如果图片宽大于高
            w = maxWidth;
            h = Math.ceil(maxWidth * height / width);
        }else{     // 如果图片高大于宽
            h = maxWidth;
            w = Math.ceil(maxWidth * width / height);
        }
        imgScale = {
            width:w,
            height:h
        }
    }
    return imgScale;
}
function dataURLtoBlob(dataurl) {
    var arr = dataurl.split(','),
        mime = arr[0].match(/:(.*?);/)[1],
        bstr = atob(arr[1]),
        n = bstr.length,
        u8arr = new Uint8Array(n);
    while (n--) {
        u8arr[n] = bstr.charCodeAt(n);
    }
    return new Blob([u8arr], { type: mime });
}