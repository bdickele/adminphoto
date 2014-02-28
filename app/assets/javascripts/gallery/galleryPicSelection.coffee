this.toggleCheckBoxes = (checkToggle) ->
    checkboxes = document.getElementById("picForm").getElementsByTagName('input')
    toggleCheckBox(item, checkToggle) for item in checkboxes

this.toggleCheckBox = (checkBox, checkToggle) ->
 if (checkBox.type == 'checkbox') then checkBox.checked = checkToggle