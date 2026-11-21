const el = id => document.getElementById(id);
let product;
let order;
const stateNames = {pay_wait: "待付款", pay_success: "已付款", close: "已取消"};

async function api(path, options) {
    const response = await fetch(path, options);
    const body = await response.json();
    if (!response.ok) throw Object.assign(new Error(body.message || "请求失败"), {status: response.status});
    return body;
}

function render() {
    const waiting = order?.status === "pay_wait";
    el("create").hidden = waiting;
    el("create").disabled = !product;
    el("pay").hidden = !waiting;
    el("cancel").hidden = !waiting;
    el("status").textContent = order ? "订单状态 · " + stateNames[order.status] : "";
    el("number").textContent = order ? "订单号 " + order.id : "";
}

async function act(action) {
    for (const button of document.querySelectorAll("button")) button.disabled = true;
    try {
        if (action === "create") {
            const requestId = sessionStorage.getItem("checkout") || crypto.randomUUID();
            sessionStorage.setItem("checkout", requestId);
            order = await api("api/orders", {method: "POST", headers: {"Content-Type": "application/json"},
                body: JSON.stringify({productId: product.id, requestId})});
            sessionStorage.setItem("order", order.id);
            sessionStorage.removeItem("checkout");
        } else {
            order = await api("api/orders/" + order.id + "/" + action, {method: "POST"});
        }
        render();
    } catch (error) {
        el("status").textContent = error.message;
    } finally {
        for (const button of document.querySelectorAll("button")) button.disabled = false;
    }
}

async function start() {
    product = await api("api/product");
    el("name").textContent = product.name;
    el("price").textContent = "¥" + product.price.toFixed(2);
    const saved = sessionStorage.getItem("order");
    if (saved) {
        try { order = await api("api/orders/" + saved); }
        catch (error) {
            if (error.status !== 404) throw error;
            sessionStorage.removeItem("order");
        }
    }
    render();
}
el("create").onclick = () => act("create");
el("pay").onclick = () => act("demo-pay");
el("cancel").onclick = () => act("cancel");
start().catch(error => { el("status").textContent = error.message; });
