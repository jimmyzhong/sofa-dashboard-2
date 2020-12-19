package xyz.lot.dashboard.common.expection.file;

import xyz.lot.common.constant.ErrCode;
import xyz.lot.common.exception.BusinessException;

public class FileSizeLimitExceededException extends BusinessException {

    public FileSizeLimitExceededException(String message) {
        super(ErrCode.FAIL_CODE, message);
    }

    public FileSizeLimitExceededException(long size) {
        super(ErrCode.FAIL_CODE, "文件大小不能超过" + size);
    }

}
