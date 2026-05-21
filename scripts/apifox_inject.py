import json, time, shutil, os, sys

src = os.path.expandvars(r'%APPDATA%\apifox\data-storage-apiDetailTreeList.json')
if not os.path.exists(src):
    print(f"文件不存在: {src}")
    sys.exit(1)

shutil.copy(src, src + '.bak')
print(f"已备份到 {src}.bak")

with open(src, 'r', encoding='utf-8') as f:
    data = json.load(f)

project = data['8111704']

# 移除已存在的"智能错题本"文件夹，避免重复
project = [item for item in project if item.get('name') != '智能错题本']
data['8111704'] = project

base_id = int(time.time() * 1000)
FOLDER_ID = base_id
API_IDS = [base_id + i for i in range(1, 12)]
CASE_IDS = [base_id + i + 100 for i in range(1, 12)]
MODULE_ID = 7522023

apis_def = [
    {
        "name": "OCR 识别题目图片",
        "method": "post",
        "path": "/api/mistake/ocr",
        "query_params": [
            {"name": "imageUrl", "required": True, "desc": "图片URL，来自上传接口返回", "example": "/uploads/images/202605/abc123.jpg", "type": "string"},
            {"name": "subject", "required": False, "desc": "科目提示（可选）", "example": "408计算机", "type": "string"}
        ],
        "response_json": '{"code":200,"message":"success","data":{"text":"OCR识别的文字内容","imageUrl":"/uploads/images/202605/abc123.jpg","suggestedSubject":"408计算机","suggestedKnowledgePoints":"操作系统-进程调度"}}'
    },
    {
        "name": "创建错题",
        "method": "post",
        "path": "/api/mistake/notes",
        "body_json": '{"subject":"408计算机","questionContent":"在操作系统中，哪种进程调度算法可能产生饥饿现象？","answer":"短作业优先(SJF)算法可能导致长作业长期得不到调度。","imageUrl":"/uploads/images/202605/abc123.jpg","knowledgePoints":"操作系统-进程调度","source":"2023真题","difficulty":3}',
        "response_json": '{"code":200,"message":"success","data":{"id":"1963837461523480576","userId":"2045115386889883651","subject":"408计算机","questionContent":"在操作系统中，哪种进程调度算法可能产生饥饿现象？","answer":"短作业优先(SJF)算法可能导致长作业长期得不到调度。","imageUrl":"/uploads/images/202605/abc123.jpg","knowledgePoints":"操作系统-进程调度","source":"2023真题","difficulty":3,"masteryLevel":0,"reviewStage":0,"reviewCount":0,"nextReviewDate":"2026-05-22","lastReviewDate":null,"createdAt":"2026-05-21T22:36:33","updatedAt":"2026-05-21T22:36:33","reviewStageText":"新录入"}}'
    },
    {
        "name": "分页查询错题本",
        "method": "get",
        "path": "/api/mistake/notes",
        "query_params": [
            {"name": "pageNum", "required": False, "desc": "页码", "example": "1", "type": "integer"},
            {"name": "pageSize", "required": False, "desc": "每页条数", "example": "10", "type": "integer"},
            {"name": "subject", "required": False, "desc": "科目筛选，不传查全部", "example": "408计算机", "type": "string"}
        ]
    },
    {
        "name": "查看错题详情",
        "method": "get",
        "path": "/api/mistake/notes/{id}",
        "path_params": [{"name": "id", "desc": "错题ID", "type": "string", "example": "1963837461523480576"}],
        "response_json": '{"code":200,"message":"success","data":{"id":"1963837461523480576","userId":"2045115386889883651","subject":"408计算机","questionContent":"在操作系统中，哪种进程调度算法可能产生饥饿现象？","answer":"短作业优先(SJF)算法可能导致长作业长期得不到调度。","imageUrl":"/uploads/images/202605/abc123.jpg","knowledgePoints":"操作系统-进程调度","source":"2023真题","difficulty":3,"masteryLevel":75,"reviewStage":1,"reviewCount":1,"nextReviewDate":"2026-05-22","lastReviewDate":null,"createdAt":"2026-05-21T22:07:20","updatedAt":"2026-05-21T22:07:20","reviewStageText":"第1次复习(1天后)"}}'
    },
    {
        "name": "更新错题",
        "method": "put",
        "path": "/api/mistake/notes",
        "body_json": '{"id":"1963837461523480576","answer":"修改后的答案...","masteryLevel":50}',
        "response_json": '{"code":200,"message":"success","data":{"id":"1963837461523480576","userId":"2045115386889883651","subject":"408计算机","questionContent":"在操作系统中，哪种进程调度算法可能产生饥饿现象？","answer":"修改后的答案...","imageUrl":"/uploads/images/202605/abc123.jpg","knowledgePoints":"操作系统-进程调度","source":"2023真题","difficulty":3,"masteryLevel":50,"reviewStage":1,"reviewCount":1,"nextReviewDate":"2026-05-22","lastReviewDate":null,"createdAt":"2026-05-21T22:07:20","updatedAt":"2026-05-21T22:36:33","reviewStageText":"第1次复习(1天后)"}}'
    },
    {
        "name": "删除错题（逻辑删除）",
        "method": "delete",
        "path": "/api/mistake/notes/{id}",
        "path_params": [{"name": "id", "desc": "错题ID", "type": "string", "example": "1963837461523480576"}]
    },
    {
        "name": "今日待复习错题列表",
        "method": "get",
        "path": "/api/mistake/review/today",
        "query_params": [
            {"name": "pageNum", "required": False, "desc": "页码", "example": "1", "type": "integer"},
            {"name": "pageSize", "required": False, "desc": "每页条数", "example": "10", "type": "integer"}
        ],
        "response_json": '{"code":200,"message":"success","data":{"total":1,"list":[{"id":"1963837461523480576","noteId":"1963837461523480600","subject":"英语(一)","questionContent":"The professor reminded the students that the assignment ____ by next Monday.","answer":"must be submitted / should be submitted","knowledgePoints":"英语-虚拟语气,英语-被动语态","difficulty":2,"masteryLevel":30,"reviewStage":0,"reviewStageText":"新录入","reviewCount":0,"isCompleted":false,"planDate":"2026-05-21"}],"pageNum":1,"pageSize":10}}'
    },
    {
        "name": "完成一道错题的复习",
        "method": "post",
        "path": "/api/mistake/review/{noteId}/complete",
        "path_params": [{"name": "noteId", "desc": "错题ID（来自今日待复习列表的noteId字段）", "type": "string", "example": "1963837461523480600"}],
        "body_json": '{"masteryAfter":60,"isCorrect":1}',
        "response_json": '{"code":200,"message":"success","data":{"noteId":"1963837461523480600","reviewStage":1,"reviewStageText":"第1次复习(1天后)","masteryLevel":60,"nextReviewDate":"2026-05-22","reviewCount":1,"isCorrect":1}}'
    },
    {
        "name": "错题本统计信息",
        "method": "get",
        "path": "/api/mistake/stats",
        "response_json": '{"code":200,"message":"success","data":{"totalNotes":6,"todayReviewCount":2,"reviewedToday":0,"avgMastery":47.5,"subjectDistribution":{"408计算机":2,"英语(一)":1,"政治":1,"数学(一)":1,"数据结构":1},"stageDistribution":{"0":3,"1":1,"2":1,"3":1}}}'
    }
]

folder_children = []

for i, api_def in enumerate(apis_def):
    api_id = API_IDS[i]
    case_id = CASE_IDS[i]
    path = api_def["path"]

    # 构建 path 参数
    path_params = api_def.get("path_params", [])
    # 构建 query 参数
    query_params = api_def.get("query_params", [])
    # 合并请求参数
    request_params = []
    for pp in path_params:
        request_params.append({
            "id": "",
            "name": pp["name"],
            "required": True,
            "desc": pp["desc"],
            "paramType": "path",
            "schema": {"type": pp.get("type", "string")},
            "example": pp.get("example", ""),
            "isDisable": False,
            "enable": True,
            "advancedSetting": {}
        })
    for qp in query_params:
        request_params.append({
            "id": "",
            "name": qp["name"],
            "required": qp.get("required", False),
            "desc": qp["desc"],
            "paramType": "query",
            "schema": {"type": qp.get("type", "string")},
            "example": qp.get("example", ""),
            "isDisable": False,
            "enable": True,
            "advancedSetting": {}
        })

    # 请求体
    body_json = api_def.get("body_json", "")
    request_body = None
    if body_json:
        request_body = {
            "type": "json",
            "parameters": [],
            "jsonSchema": {"type": "object", "properties": {}},
            "jsonString": body_json
        }

    # 响应体
    response_json = api_def.get("response_json", "")
    response_body = None
    if response_json:
        response_body = {
            "id": "",
            "code": 200,
            "name": "成功",
            "contentType": "application/json",
            "isDefault": True,
            "jsonString": response_json,
            "jsonSchema": {"type": "object", "properties": {}},
            "description": "成功"
        }

    api_node = {
        "key": f"apiDetail.{api_id}",
        "type": "apiDetail",
        "name": api_def["name"],
        "moduleId": MODULE_ID,
        "children": [
            {
                "key": f"apiCase.{case_id}",
                "type": "apiCase",
                "name": "成功",
                "moduleId": MODULE_ID,
                "children": [],
                "case": {
                    "id": case_id,
                    "name": "成功",
                    "moduleId": MODULE_ID,
                    "visibility": "INHERITED",
                    "editorId": 3106646,
                    "apiId": api_id,
                    "type": "http"
                }
            }
        ],
        "api": {
            "id": api_id,
            "name": api_def["name"],
            "moduleId": MODULE_ID,
            "type": "http",
            "method": api_def["method"],
            "path": path,
            "folderId": FOLDER_ID,
            "tags": ["智能错题本"],
            "status": "released",
            "responsibleId": 0,
            "customApiFields": {},
            "visibility": "INHERITED",
            "editorId": 3106646,
            "requestParams": {
                "parameters": request_params,
                "body": request_body,
                "header": [],
                "cookie": [],
                "auth": {"type": "inherit", "properties": {}},
                "advancedSettings": {"followRedirects": False}
            },
            "responseList": [response_body] if response_body else []
        }
    }

    folder_children.append(api_node)

folder_node = {
    "key": f"apiDetailFolder.{FOLDER_ID}",
    "type": "apiDetailFolder",
    "name": "智能错题本",
    "moduleId": MODULE_ID,
    "children": folder_children,
    "folder": {
        "id": FOLDER_ID,
        "name": "智能错题本",
        "moduleId": MODULE_ID,
        "docId": 0,
        "parentId": 0,
        "projectBranchId": 0,
        "shareSettings": {},
        "visibility": "INHERITED",
        "editorId": 3106646,
        "type": "http"
    }
}

project.append(folder_node)

with open(src, 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent='\t')

print(f"Done! 文件夹ID:{FOLDER_ID}  API IDs:{API_IDS}")
print("请关闭并重新打开 Apifox")
