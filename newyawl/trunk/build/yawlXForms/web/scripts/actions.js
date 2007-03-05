function activate(id){
    window.status= "element id clicked:" + id;
}

function setXFormsValue(id){
    window.status= "value for id changed:" + id;
}

function xf_insert (repeatid) {
    var repeat = document.getElementById(repeatid);
    var children = repeat.childNodes;
    var index = parseInt(repeat.getAttribute('index'));
    var count = 0;

    for (i = 0; i < children.length; i++) {
        var node = children[i];
        if (node.nodeType == 1) {
            count++;
            if (count == index) {
                var newNode = node.cloneNode(true);
                repeat.insertBefore(newNode, node);
                node.className = 'repeat-deselected';
                return true;
            }
        }
    }
    return false;
}

function xf_delete (repeatid) {
    var repeat = document.getElementById(repeatid);
    var children = repeat.childNodes;
    var index = parseInt(repeat.getAttribute('index'));
    var count = 0;
    var previous = null;
    var current = null;
    var next = null;

    for (i = 0; i < children.length; i++) {
        var node = children[i];
        if (node.nodeType == 1) {
            count++;
            if (count == index - 1) {
                previous = node;
            }
            if (count == index) {
                current = node;
            }
            if (count == index + 1) {
                next = node;
            }
        }
    }

    if (next != null) {
        next.className = "repeat-selected";
        repeat.removeChild(current);
        return true;
    }

    if (previous != null) {
        previous.className = "repeat-selected";
        repeat.removeChild(current);
        repeat.setAttribute('index', index - 1);
        return true;
    }

    repeat.removeChild(current);
    return true;
}

function xf_setindex (repeatid, index) {
    var repeat = document.getElementById(repeatid);
    repeat.setAttribute('index', index);
}

function xf_select_repeat_item (repeatid, repeatitem) {
    var repeat = document.getElementById(repeatid);
    var children = repeat.childNodes;
    var count = 0;
    var position = 0;

    for (i = 0; i < children.length; i++) {
        var node = children[i];
        if (node.nodeType == 1) {
            count++;
            node.className = 'repeat-deselected';

            if (node == repeatitem) {
                node.className = 'repeat-selected';
                position = count;
            }

        }
    }

    repeat.setAttribute('index', position);
    return true;
}
