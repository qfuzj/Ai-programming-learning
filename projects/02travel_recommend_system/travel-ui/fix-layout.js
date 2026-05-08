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

  // Match the entire page-head div
  // Assuming it starts at <div class="page-head"> and ends at the first </div>\n    </div>
  const headRegex = /<div class="page-head">([\s\S]*?)<\/div>\n {4}<\/div>/;
  const match = content.match(headRegex);

  if (!match) {
    console.log("No match for", file);
    return;
  }

  const inner = match[1];

  // Extract actions if present
  let actions = "";
  const btnMatch = inner.match(/<el-button[\s\S]*?<\/el-button>/g);
  const headActionsMatch = inner.match(/<div class="head-actions">([\s\S]*?)<\/div>/);

  if (headActionsMatch) {
    actions = headActionsMatch[1].trim();
  } else if (btnMatch) {
    // Collect standalone buttons in page-head
    actions = btnMatch.join("\n        ");
  }

  // Remove the page-head
  content = content.replace(headRegex, "").replace(/^\s*\n/m, ""); // remove the empty line

  // Now, inject actions into filter-panel if needed
  if (actions) {
    const actionHtml = `\n      <div class="action-bar">\n        ${actions}\n      </div>`;
    // Find the end of filter-panel
    const filterEndIndex = content.lastIndexOf("</el-form>");
    if (filterEndIndex !== -1) {
      content =
        content.slice(0, filterEndIndex) + actionHtml + "\n    " + content.slice(filterEndIndex);
    }
  }

  fs.writeFileSync(path, content);
  console.log("Processed", file);
});
