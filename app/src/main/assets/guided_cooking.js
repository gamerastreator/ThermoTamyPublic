// Guided cooking javascript
function toggleGuidedCooking() {
    var ingredients = document.querySelectorAll('.ingredient');
    var steps = document.querySelectorAll('.step');

    ingredients.forEach(function(ingredient) {
        var checkbox = ingredient.querySelector('.guided-cooking-checkbox');
        if (checkbox) {
            checkbox.remove();
        } else {
            checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.className = 'guided-cooking-checkbox';
            checkbox.addEventListener('change', function() {
                if (this.checked) {
                    ingredient.style.display = 'none';
                }
            });
            ingredient.insertBefore(checkbox, ingredient.firstChild);
        }
    });

    steps.forEach(function(step) {
        var checkbox = step.querySelector('.guided-cooking-checkbox');
        if (checkbox) {
            checkbox.remove();
        } else {
            checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.className = 'guided-cooking-checkbox';
            checkbox.addEventListener('change', function() {
                if (this.checked) {
                    step.style.display = 'none';
                }
            });
            step.insertBefore(checkbox, step.firstChild);
        }
    });
}
