package com.zzu.kaoyan.module.schoolselect.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("school_info")
public class SchoolInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String level;
    private String location;
    private String logoUrl;
    private String website;
    private Boolean isSelfLine;
    private Integer avgAdmissionScore;
    private Integer minAdmissionScore;
    private Integer hotLevel;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
