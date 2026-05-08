// eslint-disable-next-line @typescript-eslint/no-require-imports
const fs = require("fs");
const files = [
  "AuditManageView.vue",
  "LogView.vue",
  "RegionManageView.vue",
  "ScenicManageView.vue",
  "SystemConfigView.vue",
  "TagManageView.vue",
  "UserManageView.vue",
];

files.forEach((file) => {
  const path = "src/views/admin/" + file;
  let content = fs.readFileSync(path, "utf8");

  const headRegex = /<div class="page-head">([\s\S]*?)<\/div>\n {4}<\/div>\n*/;
  const match = content.match(headRegex);

  if (!match) {
    console.log("No match for", file);
    return;
  }

  const inner = match[1];

  let actions = "";
  const headActionsMatch = inner.match(/<div class="head-actions">\s*([\s\S]*?)\s*<\/div>/);

  if (headActionsMatch) {
    actions = headActionsMatch[1].trim();
  } else {
    // If no <div class="head-actions">, let's just grab the buttons directly
    // Sometimes button is outside div
    const btnRegex = /<el-button[\s\S]*?<\/el-button>/g;
    const btns = [];
    let bMatch;
    while ((bMatch = btnRegex.exec(inner)) !== null) {
      btns.push(bMatch[0]);
    }
    if (btns.length > 0) {
      actions = btns.join("\n        ");
    }
  }

  content = content.replace(headRegex, "");

  if (actions) {
    const actionHtml = `\n      <div class="action-bar">\n        ${actions}\n      </div>`;
    const filterEndIndex = content.lastIndexOf("</el-form>");
    if (filterEndIndex !== -1) {
      content =
        content.slice(0, filterEndIndex) + actionHtml + "\n    " + content.slice(filterEndIndex);
    }
  }

  fs.writeFileSync(path, content);
  console.log("Processed", file);
});
